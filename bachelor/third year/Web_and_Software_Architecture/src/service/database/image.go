package database

import (
	"fmt"
)

// UpdateUserPhoto updates the photo for a given user
func (db *appdbimpl) UpdateUserPhoto(userId int64, photoData []byte) error {
	// Prepare the SQL query to update the user's photo
	_, err := db.c.Exec(`
		UPDATE users 
		SET photo = ? 
		WHERE id = ?`, photoData, userId)
	if err != nil {
		return fmt.Errorf("error updating user photo: %w", err)
	}

	return nil
}

// UpdateGroupPhoto updates the photo for a given group
func (db *appdbimpl) UpdateGroupPhoto(conversationId int64, photoData []byte) error {
	// Prepare the SQL query to update the group's photo
	_, err := db.c.Exec(`
		UPDATE conversations 
		SET photo = ? 
		WHERE conversation_id = ?`, photoData, conversationId)
	if err != nil {
		return fmt.Errorf("error updating group photo: %w", err)
	}

	return nil
}
