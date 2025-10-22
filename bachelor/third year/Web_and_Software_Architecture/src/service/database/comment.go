package database

import "fmt"

// AddCommentToMessage aggiunge un commento a un messaggio nel database.
func (db *appdbimpl) AddCommentToMessage(messageId int64, senderId int64, content string) (Comment, error) {
	sender, err := db.GetUserById(senderId)
	if err != nil {
		return Comment{}, fmt.Errorf("error fetching sender details: %w", err)
	}

	result, err := db.c.Exec(
		"INSERT INTO comments (message_id, sender_id, content) VALUES (?, ?, ?)",
		messageId, senderId, content,
	)
	if err != nil {
		return Comment{}, fmt.Errorf("failed to add comment to message: %w", err)
	}

	commentId, err := result.LastInsertId()
	if err != nil {
		return Comment{}, fmt.Errorf("error retrieving last insert id: %w", err)
	}

	return Comment{
		CommentId: commentId,
		Content:   content,
		Sender:    sender,
	}, nil
}

// GetCommentById retrieves a comment by its ID.
func (db *appdbimpl) GetCommentById(commentId int64) (Comment, error) {
	var comment Comment
	var senderId int64

	query := `
		SELECT comment_id, sender_id, content
		FROM comments
		WHERE comment_id = ?
	`
	err := db.c.QueryRow(query, commentId).Scan(&comment.CommentId, &senderId, &comment.Content)
	if err != nil {
		return Comment{}, fmt.Errorf("error retrieving comment: %w", err)
	}

	// Fetch user details from the database
	sender, err := db.GetUserById(senderId)
	if err != nil {
		return Comment{}, fmt.Errorf("error fetching sender details: %w", err)
	}
	comment.Sender = sender
	return comment, nil
}

// DeleteCommentById deletes a comment by its ID.
func (db *appdbimpl) DeleteCommentById(commentId int64) error {
	query := `
		DELETE FROM comments
		WHERE comment_id = ?
	`
	_, err := db.c.Exec(query, commentId)
	if err != nil {
		return fmt.Errorf("error deleting comment: %w", err)
	}
	return nil
}
