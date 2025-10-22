package database

import (
	"database/sql"
	"errors"
	"fmt"
)

func (db *appdbimpl) CreateGroup(conversation Conversation) error {
	_, err := db.c.Exec(
		`INSERT INTO conversations (name, photo_url) VALUES (?, ?)`,
		conversation.Name, conversation.Photo,
	)
	if err != nil {
		return fmt.Errorf("failed to create group: %w", err)
	}
	return nil
}

func (db *appdbimpl) GetGroupById(conversationId int64) (*Conversation, error) {
	row := db.c.QueryRow(
		`SELECT conversation_id, name, photo FROM conversations WHERE conversation_id = ?`,
		conversationId,
	)
	var conversation Conversation
	if err := row.Scan(&conversation.ConversationId, &conversation.Name, &conversation.Photo); err != nil {
		if errors.Is(err, sql.ErrNoRows) {
			return nil, fmt.Errorf("group not found: %w", err)
		}
		return nil, fmt.Errorf("error retrieving group: %w", err)
	}
	return &conversation, nil
}

func (db *appdbimpl) AddUserToGroup(conversationId int64, userId int64) error {
	_, err := db.c.Exec(
		`INSERT INTO conversation_members (conversation_id, user_id) VALUES (?, ?)`,
		conversationId, userId,
	)
	if err != nil {
		return fmt.Errorf("failed to add user to group: %w", err)
	}
	return nil
}

// IsUserMemberOfGroup checks if a user is a member of a group
func (db *appdbimpl) IsUserMemberOfGroup(userId int64, conversationId int64) (bool, error) {
	var exists bool
	query := "SELECT EXISTS (SELECT 1 FROM conversation_members WHERE conversation_id = ? AND user_id = ?)"
	err := db.c.QueryRow(query, conversationId, userId).Scan(&exists)
	return exists, err
}

// RemoveUserFromGroup removes a user from a group
func (db *appdbimpl) RemoveUserFromGroup(conversationId int64, userId int64) error {
	_, err := db.c.Exec("DELETE FROM conversation_members WHERE conversation_id = ? AND user_id = ?", conversationId, userId)
	return err
}

// UpdateGroupName change the name of an exsiting group
func (db *appdbimpl) UpdateGroupName(conversationId int64, newName string) error {
	_, err := db.c.Exec("UPDATE conversations SET name = ? WHERE conversation_id = ?", newName, conversationId)
	if err != nil {
		return fmt.Errorf("failed to update group name: %w", err)
	}
	return nil
}

func (db *appdbimpl) GetGroupMembers(groupId int64) ([]string, error) {
	// Query to get group members
	rows, err := db.c.Query(`
		SELECT u.name
		FROM users u
		INNER JOIN conversation_members cm ON u.id = cm.user_id
		WHERE cm.conversation_id = ?
	`, groupId)
	if err != nil {
		return nil, fmt.Errorf("failed to query group members: %w", err)
	}
	defer rows.Close()

	// Read the query results
	var members []string
	for rows.Next() {
		var username string
		if err := rows.Scan(&username); err != nil {
			return nil, fmt.Errorf("failed to scan group member: %w", err)
		}
		members = append(members, username)
	}

	if err := rows.Err(); err != nil {
		return nil, fmt.Errorf("error iterating over group members: %w", err)
	}

	return members, nil
}
