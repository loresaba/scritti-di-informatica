package database

import (
	"database/sql"
	"errors"
	"fmt"
	"time"
)

// Conversation represent a conversation object
type Conversation struct {
	ConversationId int64        `json:"conversationId"`
	Name           string       `json:"name"`
	Photo          []byte       `json:"photo,omitempty"`
	LastMessage    *LastMessage `json:"lastMessage,omitempty"`
	Type           string       `json:"type"`
}

// LastMessage represents the details of the last message in a conversation
type LastMessage struct {
	MessageID int64     `json:"messageId"`
	Timestamp time.Time `json:"timestamp"`
	Preview   string    `json:"preview"`
}

// GetConversationsByUser gets the conversations for a specified user
func (db *appdbimpl) GetConversationsByUser(userId int64, sortOrder string) ([]Conversation, error) {
	// Query SQL for get the conversation
	query := `
		SELECT 
			c.conversation_id, 
			c.name, 
			c.photo, 
			COALESCE(m.message_id, 0) AS message_id,
    		COALESCE(m.timestamp, NULL) AS timestamp,
    		COALESCE(m.text, '') AS content,
			c.type
		FROM 
			conversations c
		INNER JOIN 
			conversation_members cm 
		ON 
			c.conversation_id = cm.conversation_id
		LEFT JOIN 
			messages m 
		ON 
			c.last_message_id = m.message_id
			AND c.conversation_id = m.conversation_id
		WHERE 
			cm.user_id = ?
		ORDER BY 
			m.timestamp ` + sortOrder

	rows, err := db.c.Query(query, userId)
	if err != nil {
		return nil, fmt.Errorf("error fetching conversations: %w", err)
	}
	defer rows.Close()

	// Parsing of the results
	var conversations []Conversation
	for rows.Next() {
		var conversation Conversation
		var lastMessage LastMessage
		var timestampStr *string
		var photo []byte

		err := rows.Scan(
			&conversation.ConversationId,
			&conversation.Name,
			&photo,
			&lastMessage.MessageID,
			&timestampStr,
			&lastMessage.Preview,
			&conversation.Type,
		)
		if err != nil {
			return nil, fmt.Errorf("error scanning conversation row: %w", err)
		}

		// If photo is not NULL, set it to the conversation's Photo field
		if len(photo) == 0 {
			conversation.Photo = nil
		} else {
			conversation.Photo = photo
		}

		// Parsing timestamp
		if timestampStr != nil {
			lastMessage.Timestamp, err = ParseTimestamp(*timestampStr)
			if err != nil {
				return nil, fmt.Errorf("error parsing timestamp: %w", err)
			}
		}

		// Ad the message only if exists
		if lastMessage.MessageID == 0 {
			conversation.LastMessage = nil
		} else {
			conversation.LastMessage = &lastMessage
		}

		// If the conversation is direct, set the name and photo of the other user
		if conversation.Type == "direct" {
			otherUser, err := db.GetOtherUserInConversation(conversation.ConversationId, userId)
			if err != nil {
				return nil, fmt.Errorf("error fetching other user in direct conversation: %w", err)
			}
			conversation.Name = otherUser.Name
			if otherUser.Photo != nil && len(otherUser.Photo) > 0 {
				photoBytes := []byte(otherUser.Photo)
				conversation.Photo = photoBytes
			}
		}

		conversations = append(conversations, conversation)
	}

	if err := rows.Err(); err != nil {
		return nil, fmt.Errorf("rows iteration error: %w", err)
	}

	return conversations, nil
}

// GetConversationById retrieves the conversation details by its ID
func (db *appdbimpl) GetConversationById(conversationId int64) (Conversation, error) {
	// Query to retrieve the conversation details
	query := `
		SELECT 
			c.conversation_id, 
			c.name, 
			c.photo, 
			COALESCE(m.message_id, 0) AS message_id, 
			COALESCE(m.timestamp, NULL) AS timestamp, 
			COALESCE(m.text, '') AS content,
			c.type
		FROM 
			conversations c
		LEFT JOIN 
			messages m 
		ON 
			c.last_message_id = m.message_id
		WHERE 
			c.conversation_id = ?`

	var conversation Conversation
	var lastMessage LastMessage
	var timestampStr *string
	var photo sql.NullByte

	// Execute the query
	row := db.c.QueryRow(query, conversationId)

	// Scan the result into the Conversation structure
	err := row.Scan(
		&conversation.ConversationId,
		&conversation.Name,
		&photo,
		&lastMessage.MessageID,
		&timestampStr,
		&lastMessage.Preview,
		&conversation.Type,
	)
	if err != nil {
		if errors.Is(err, sql.ErrNoRows) {
			return Conversation{}, fmt.Errorf("conversation not found")
		}
		return Conversation{}, fmt.Errorf("error scanning conversation: %w", err)
	}

	// If photo is not NULL, set it to the conversation's Photo field
	if photo.Valid {
		conversation.Photo = []byte{photo.Byte}
	}

	// Parsing timestamp
	if timestampStr != nil {
		lastMessage.Timestamp, err = ParseTimestamp(*timestampStr)
		if err != nil {
			return Conversation{}, fmt.Errorf("error parsing timestamp: %w", err)
		}
	}

	// Ad the message only if exists
	if lastMessage.MessageID == 0 {
		conversation.LastMessage = nil
	} else {
		conversation.LastMessage = &lastMessage
	}

	return conversation, nil
}

// CreateConversation creates a new conversation between two users
func (db *appdbimpl) CreateConversation(user1Id, user2Id int64, conversationType string) (Conversation, error) {
	// Start a transaction to ensure atomicity
	tx, err := db.c.Begin()
	if err != nil {
		return Conversation{}, fmt.Errorf("error starting transaction: %w", err)
	}
	defer func() { // Ensure rollback in case of failure
		if err := tx.Rollback(); err != nil && err != sql.ErrTxDone {
			fmt.Errorf("error rolling back transaction: %w", err)
		}
	}()

	// Determine the name of the conversation based on type
	var conversationName string
	if conversationType == "direct" {
		// Get the target user's name for direct conversation
		targetUser, err := db.GetUserById(user2Id)
		if err != nil {
			return Conversation{}, fmt.Errorf("error fetching target user: %w", err)
		}
		conversationName = targetUser.Name
	} else {
		conversationName = "New Conversation"
	}

	// Create the new conversation
	result, err := tx.Exec(`
		INSERT INTO conversations (name, last_message_id, type)
		VALUES (?, ?, ?)
	`, conversationName, nil, conversationType)
	if err != nil {
		return Conversation{}, fmt.Errorf("error inserting conversation: %w", err)
	}

	// Get the conversation ID
	conversationId, err := result.LastInsertId()
	if err != nil {
		return Conversation{}, fmt.Errorf("error retrieving conversation ID: %w", err)
	}

	// Add the two users to the conversation
	_, err = tx.Exec(`
		INSERT INTO conversation_members (conversation_id, user_id)
		VALUES (?, ?), (?, ?)
	`, conversationId, user1Id, conversationId, user2Id)
	if err != nil {
		return Conversation{}, fmt.Errorf("error adding users to conversation: %w", err)
	}

	// Commit the transaction
	if err := tx.Commit(); err != nil {
		return Conversation{}, fmt.Errorf("error committing transaction: %w", err)
	}

	// Fetch the conversation
	conversation, err := db.GetConversationById(conversationId)
	if err != nil {
		return Conversation{}, fmt.Errorf("error fetching conversation details: %w", err)
	}

	return conversation, nil
}

// ParseTimestamp converts a datetime string to time.Time
func ParseTimestamp(datetimeStr string) (time.Time, error) {
	if datetimeStr == "" {
		// Return a zero value of time.Time if the string is empty
		return time.Time{}, nil
	}

	// Handle the specific case for the "epoch" timestamp without timezone
	if datetimeStr == "1970-01-01 00:00:00" {
		// Return the zero value of time.Time (1970-01-01 00:00:00 UTC)
		return time.Time{}, nil
	}

	// Timestamp format that includes the timezone
	const layout = "2006-01-02 15:04:05.999999999-07:00"
	parsedTime, err := time.Parse(layout, datetimeStr)
	if err != nil {
		// Return an error if parsing fails
		return time.Time{}, fmt.Errorf("error parsing timestamp: %w", err)
	}
	return parsedTime, nil
}

// GetOtherUserInConversation retrieves the other user in a direct conversation
func (db *appdbimpl) GetOtherUserInConversation(conversationId, userId int64) (*User, error) {
	var otherUser User
	var photo []byte

	// Query to find the other user in the conversation
	query := `
		SELECT u.id, u.name, u.photo
		FROM conversation_members cm
		INNER JOIN users u ON cm.user_id = u.id
		WHERE cm.conversation_id = ? AND cm.user_id != ?
	`

	err := db.c.QueryRow(query, conversationId, userId).Scan(
		&otherUser.UserId,
		&otherUser.Name,
		&photo,
	)
	if errors.Is(err, sql.ErrNoRows) {
		return nil, fmt.Errorf("no other user found in conversation %d for user %d", conversationId, userId)
	}
	if err != nil {
		return nil, fmt.Errorf("error retrieving other user: %w", err)
	}

	// If photo is valid, assign the photo as a byte slice
	if len(photo) == 0 {
		otherUser.Photo = nil
	} else {
		otherUser.Photo = photo
	}

	return &otherUser, nil
}

func (db *appdbimpl) UpdateLastAccess(userId int64, conversationId int64) error {
	if userId <= 0 || conversationId <= 0 {
		return fmt.Errorf("invalid userId (%d) or conversationId (%d)", userId, conversationId)
	}

	query := `
		UPDATE conversation_members
		SET last_access = ?
		WHERE user_id = ? AND conversation_id = ?;
	`

	timestamp := time.Now()
	result, err := db.c.Exec(query, timestamp, userId, conversationId)
	if err != nil {
		return fmt.Errorf("failed to update last_access: %w", err)
	}

	rowsAffected, err := result.RowsAffected()
	if err != nil {
		return fmt.Errorf("failed to get affected rows: %w", err)
	}

	if rowsAffected == 0 {
		return fmt.Errorf("no rows updated, possibly invalid userId (%d) or conversationId (%d)", userId, conversationId)
	}

	return nil
}
