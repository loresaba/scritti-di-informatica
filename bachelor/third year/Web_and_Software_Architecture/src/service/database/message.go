package database

import (
	"database/sql"
	"errors"
	"fmt"
	"time"
)

// Message represents a single message in a conversation.
type Message struct {
	MessageId        int64     `json:"id"`
	Timestamp        time.Time `json:"timestamp"`
	Text             string    `json:"text,omitempty"`
	Photo            []byte    `json:"photo,omitempty"`
	Sender           User      `json:"sender"`
	Status           string    `json:"status"`
	Comments         []Comment `json:"comments,omitempty"`
	Type             string    `json:"type"`
	ReplyToMessageId *int64    `json:"replyToMessageId,omitempty"`
}

// Comment represents a comment on a message.
type Comment struct {
	CommentId int64  `json:"commentId"`
	Sender    User   `json:"sender"`
	Content   string `json:"content"`
}

// GetMessagesByConversation retrieves the messages for a specific conversation, sorted by timestamp.
func (db *appdbimpl) GetMessagesByConversation(userId int64, conversationId int64, sortOrder string) ([]Message, error) {
	// Prepare the query to retrieve messages from the database, ordered by timestamp.
	var orderBy string
	if sortOrder == "asc" {
		orderBy = "ASC"
	} else {
		orderBy = "DESC"
	}

	rows, err := db.c.Query(`
		SELECT m.message_id, m.timestamp, m.text, m.sender_id, m.status, m.type, m.reply_to_message_id, m.photo
		FROM messages m
		JOIN conversations c ON c.conversation_id = m.conversation_id
		JOIN conversation_members cm ON cm.conversation_id = c.conversation_id
		WHERE cm.user_id = ? AND c.conversation_id = ?
		ORDER BY m.timestamp `+orderBy, userId, conversationId)
	if err != nil {
		return nil, fmt.Errorf("error retrieving messages: %w", err)
	}
	defer rows.Close()

	// Update the last access timestamp for the user in the conversation
	if err := db.UpdateLastAccess(userId, conversationId); err != nil {
		return nil, fmt.Errorf("failed to update last access: %w", err)
	}

	var messages []Message
	for rows.Next() {
		var msg Message
		var senderId int64
		var replyToMessageId sql.NullInt64
		var photo sql.RawBytes
		if err := rows.Scan(&msg.MessageId, &msg.Timestamp, &msg.Text, &senderId, &msg.Status, &msg.Type, &replyToMessageId, &photo); err != nil {
			return nil, fmt.Errorf("error scanning message: %w", err)
		}

		// Retrieve the sender's user data
		sender, err := db.GetUserById(senderId)
		if err != nil {
			if errors.Is(err, sql.ErrNoRows) {
				return nil, fmt.Errorf("sender not found for message %d: %w", msg.MessageId, err)
			}
			return nil, fmt.Errorf("error retrieving sender for message %d: %w", msg.MessageId, err)
		}
		msg.Sender = sender

		// Retrieve the comments for the message (if any)
		msg.Comments, err = db.GetCommentsByMessage(msg.MessageId)
		if err != nil {
			return nil, fmt.Errorf("error retrieving comments for message %d: %w", msg.MessageId, err)
		}

		// Handle nullable ReplyToMessageId
		if replyToMessageId.Valid {
			msg.ReplyToMessageId = &replyToMessageId.Int64
		}

		// Store the photo as a byte slice if it exists
		if photo != nil {
			msg.Photo = photo
		}

		messages = append(messages, msg)
	}

	if err := rows.Err(); err != nil {
		return nil, fmt.Errorf("rows iteration error: %w", err)
	}

	// Update message status
	messages, err = db.UpdateMessagesStatus(conversationId, messages)
	if err != nil {
		return nil, fmt.Errorf("error updating message statuses: %w", err)
	}

	return messages, nil
}

// GetCommentsByMessage retrieves the comments for a specific message.
func (db *appdbimpl) GetCommentsByMessage(messageId int64) ([]Comment, error) {
	rows, err := db.c.Query(`
		SELECT comment_id, sender_id, content
		FROM comments
		WHERE message_id = ?`, messageId)
	if err != nil {
		return nil, fmt.Errorf("error retrieving comments: %w", err)
	}
	defer rows.Close()

	var comments []Comment
	for rows.Next() {
		var comment Comment
		var senderId int64
		if err := rows.Scan(&comment.CommentId, &senderId, &comment.Content); err != nil {
			return nil, fmt.Errorf("error scanning comment: %w", err)
		}

		// Retrieve the sender's user data
		sender, err := db.GetUserById(senderId)
		if err != nil {
			if errors.Is(err, sql.ErrNoRows) {
				return nil, fmt.Errorf("sender not found for comment: %w", err)
			}
			return nil, fmt.Errorf("error retrieving sender for comment: %w", err)
		}
		comment.Sender = sender
		comments = append(comments, comment)

	}

	if err := rows.Err(); err != nil {
		return nil, fmt.Errorf("rows iteration error: %w", err)
	}

	return comments, nil
}

// AddMessage adds a new message to the database.
func (db *appdbimpl) AddMessage(conversationId int64, senderId int64, text string, status string, messageType string, photo []byte) (Message, error) {
	sender, err := db.GetUserById(senderId)
	if err != nil {
		return Message{}, fmt.Errorf("error fetching sender details: %w", err)
	}

	timestamp := time.Now()
	var result sql.Result
	if messageType == "photo" {
		// Handle photo message
		result, err = db.c.Exec(`
			INSERT INTO messages (timestamp, text, conversation_id, sender_id, status, type, photo) 
			VALUES (?, ?, ?, ?, ?, ?, ?)`, timestamp, text, conversationId, senderId, status, "standard", photo)
	} else {
		// Handle text message
		result, err = db.c.Exec(`
			INSERT INTO messages (timestamp, text, conversation_id, sender_id, status, type) 
			VALUES (?, ?, ?, ?, ?, ?)`, timestamp, text, conversationId, senderId, status, "standard")
	}

	if err != nil {
		return Message{}, fmt.Errorf("error inserting message: %w", err)
	}

	messageId, err := result.LastInsertId()
	if err != nil {
		return Message{}, fmt.Errorf("error retrieving last insert id: %w", err)
	}

	// Update the conversation's last_message_id
	if err := db.UpdateLastMessageId(conversationId, messageId); err != nil {
		return Message{}, fmt.Errorf("error updating last_message_id: %w", err)
	}

	var msg Message
	if messageType == "photo" {
		msg.Photo = photo
	} else {
		msg.Text = text
	}

	return Message{
		MessageId: messageId,
		Timestamp: timestamp,
		Text:      text,
		Sender:    sender,
		Status:    status,
		Type:      "standard",
		Photo:     msg.Photo,
	}, nil
}

// GetMessageById retrieves a message by its ID.
func (db *appdbimpl) GetMessageById(messageId int64, conversationId int64) (Message, error) {
	var msg Message
	var senderId int64
	var replyToMessageId sql.NullInt64
	var photo []byte
	err := db.c.QueryRow(`
		SELECT message_id, timestamp, text, sender_id, status, type, reply_to_message_id, photo
		FROM messages
		WHERE message_id = ? AND conversation_id = ?`, messageId, conversationId).Scan(
		&msg.MessageId, &msg.Timestamp, &msg.Text, &senderId, &msg.Status, &msg.Type, &replyToMessageId, &photo)

	if err != nil {
		if errors.Is(err, sql.ErrNoRows) {
			return Message{}, fmt.Errorf("message not found: %w", err)
		}
		return Message{}, fmt.Errorf("error retrieving message: %w", err)
	}

	// Retrieve sender details
	sender, err := db.GetUserById(senderId)
	if err != nil {
		return Message{}, fmt.Errorf("error retrieving sender: %w", err)
	}
	msg.Sender = sender

	// Handle nullable ReplyToMessageId
	if replyToMessageId.Valid {
		msg.ReplyToMessageId = &replyToMessageId.Int64
	}

	// Store the photo as a byte slice if it exists
	if len(photo) > 0 {
		msg.Photo = photo
	}

	return msg, nil
}

// ForwardMessage forward a message into a conversation.
func (db *appdbimpl) ForwardMessage(userId int64, originalMessage Message, targetConversationId int64) (Message, error) {
	// Retrieve sender details
	sender, err := db.GetUserById(userId)
	if err != nil {
		return Message{}, fmt.Errorf("error retrieving sender: %w", err)
	}
	timestamp := time.Now()
	// Prepare the query to insert the forwarded message
	var result sql.Result
	if originalMessage.Photo != nil {
		// If the original message has a photo, include it in the insert
		result, err = db.c.Exec(`
			INSERT INTO messages (timestamp, text, conversation_id, sender_id, status, type, photo)
			VALUES (?, ?, ?, ?, ?, ?, ?)`,
			timestamp, originalMessage.Text, targetConversationId, userId, "received", "forward", originalMessage.Photo)
	} else {
		// If no photo, proceed with only the text content
		result, err = db.c.Exec(`
			INSERT INTO messages (timestamp, text, conversation_id, sender_id, status, type)
			VALUES (?, ?, ?, ?, ?, ?)`,
			timestamp, originalMessage.Text, targetConversationId, userId, "received", "forward")
	}
	if err != nil {
		return Message{}, fmt.Errorf("error forwarding message: %w", err)
	}

	newMessageId, err := result.LastInsertId()
	if err != nil {
		return Message{}, fmt.Errorf("error retrieving last insert id: %w", err)
	}

	// Update the last_message_id of the target conversation
	if err := db.UpdateLastMessageId(targetConversationId, newMessageId); err != nil {
		return Message{}, fmt.Errorf("error updating last_message_id for target conversation: %w", err)
	}

	// Create the new forwarded message
	forwardedMessage := Message{
		MessageId: newMessageId,
		Timestamp: timestamp,
		Text:      originalMessage.Text,
		Sender:    sender,
		Status:    "received",
		Type:      "forward",
	}

	// If the original message contained a photo, include it in the forwarded message
	if originalMessage.Photo != nil {
		forwardedMessage.Photo = originalMessage.Photo
	}

	return forwardedMessage, nil
}

// ReplyMessage adds a reply to an existing message with either text or a photo.
func (db *appdbimpl) ReplyMessage(conversationId int64, senderId int64, replyMessageId int64, text string, status string, messageType string, photo []byte) (Message, error) {
	sender, err := db.GetUserById(senderId)
	if err != nil {
		return Message{}, fmt.Errorf("error fetching sender details: %w", err)
	}

	timestamp := time.Now()
	var result sql.Result

	if messageType == "photo" {
		// Handle photo reply message
		result, err = db.c.Exec(`
			INSERT INTO messages (timestamp, text, conversation_id, sender_id, status, type, reply_to_message_id, photo) 
			VALUES (?, ?, ?, ?, ?, ?, ?, ?)`, timestamp, "", conversationId, senderId, status, "reply", replyMessageId, photo)
	} else {
		// Handle text reply message
		result, err = db.c.Exec(`
			INSERT INTO messages (timestamp, text, conversation_id, sender_id, status, type, reply_to_message_id) 
			VALUES (?, ?, ?, ?, ?, ?, ?)`, timestamp, text, conversationId, senderId, status, "reply", replyMessageId)
	}

	if err != nil {
		return Message{}, fmt.Errorf("error inserting reply message: %w", err)
	}

	messageId, err := result.LastInsertId()
	if err != nil {
		return Message{}, fmt.Errorf("error retrieving last insert id: %w", err)
	}

	// Update the conversation's last_message_id
	if err := db.UpdateLastMessageId(conversationId, messageId); err != nil {
		return Message{}, fmt.Errorf("error updating last_message_id: %w", err)
	}

	var msg Message
	if messageType == "photo" {
		msg.Photo = photo
	} else {
		msg.Text = text
	}

	return Message{
		MessageId:        messageId,
		Timestamp:        timestamp,
		Text:             msg.Text,
		Sender:           sender,
		Status:           status,
		Type:             "reply",
		ReplyToMessageId: &replyMessageId,
		Photo:            msg.Photo,
	}, nil
}

// UpdateLastMessageId update the filed last_message_id in the conversations table
func (db *appdbimpl) UpdateLastMessageId(conversationId int64, messageId int64) error {
	_, err := db.c.Exec(`
		UPDATE conversations
		SET last_message_id = ?
		WHERE conversation_id = ?`, messageId, conversationId)
	if err != nil {
		return fmt.Errorf("error updating last_message_id: %w", err)
	}
	return nil
}

// DeleteMessageById deletes a message by its ID
func (db *appdbimpl) DeleteMessageById(messageId int64) error {
	// Delete comments associated with the message first
	if err := db.deleteCommentsByMessageId(messageId); err != nil {
		return fmt.Errorf("error deleting comments: %w", err)
	}

	// Check if the deleted message is the last message in any conversation
	var conversationId int64
	err := db.c.QueryRow(`
		SELECT conversation_id
		FROM conversations
		WHERE last_message_id = ?`, messageId).Scan(&conversationId)

	// If no conversation is found with this last_message_id, just delete the message
	if errors.Is(err, sql.ErrNoRows) {
		// Message is not the last message in any conversation
		return db.deleteMessage(messageId)
	} else if err != nil {
		// Error querying for the conversation
		return fmt.Errorf("error checking if message is last message: %w", err)
	}

	// The message is the last message, so we need to update the conversation's last_message_id
	// Delete the message first
	if err := db.deleteMessage(messageId); err != nil {
		return err
	}

	// Find the new last message by timestamp (most recent)
	var newLastMessageId int64
	err = db.c.QueryRow(`
		SELECT message_id
		FROM messages
		WHERE conversation_id = ?
		ORDER BY timestamp DESC
		LIMIT 1`, conversationId).Scan(&newLastMessageId)

	if err != nil {
		// If no other messages exist set last_message_id to NULL
		if errors.Is(err, sql.ErrNoRows) {
			return db.UpdateLastMessageId(conversationId, 0) // Set to NULL or 0
		}
		return fmt.Errorf("error finding new last message: %w", err)
	}

	// Update the conversation with the new last_message_id
	if err := db.UpdateLastMessageId(conversationId, newLastMessageId); err != nil {
		return fmt.Errorf("error updating last_message_id after deletion: %w", err)
	}

	return nil
}

// Function to delete the message
func (db *appdbimpl) deleteMessage(messageId int64) error {
	query := `
		DELETE FROM messages
		WHERE message_id = ?`
	_, err := db.c.Exec(query, messageId)
	if err != nil {
		return fmt.Errorf("error deleting message: %w", err)
	}
	return nil
}

// Function to delete the comments associated with a message
func (db *appdbimpl) deleteCommentsByMessageId(messageId int64) error {
	query := `
		DELETE FROM comments
		WHERE message_id = ?`
	_, err := db.c.Exec(query, messageId)
	if err != nil {
		return fmt.Errorf("error deleting comments: %w", err)
	}
	return nil
}

// UpdateMessageStatus updates messages status
func (db *appdbimpl) UpdateMessagesStatus(conversationId int64, messages []Message) ([]Message, error) {
	// Get the minimum timestamp in conversation_members for the conversation
	var minTimestampStr string
	err := db.c.QueryRow(`
		SELECT CASE
			WHEN COUNT(last_access) < COUNT(*) THEN '1970-01-01 00:00:00'
			ELSE MIN(last_access)
		END
		FROM conversation_members
		WHERE conversation_id = ?`, conversationId).Scan(&minTimestampStr)
	if err != nil {
		return nil, fmt.Errorf("error retrieving minimum last_access timestamp: %w", err)
	}

	// Use ParseTimestamp to transform the string in time.Time
	minTimestamp, err := ParseTimestamp(minTimestampStr)
	if err != nil {
		return nil, fmt.Errorf("error parsing timestamp: %w", err)
	}

	// Update the status of each message
	for i, msg := range messages {
		if msg.Timestamp.Before(minTimestamp) || msg.Timestamp.Equal(minTimestamp) {
			messages[i].Status = "read"
		} else {
			messages[i].Status = "received"
		}
	}

	return messages, nil
}
