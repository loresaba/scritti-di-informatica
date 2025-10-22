package database

import (
	"database/sql"
	"errors"
	"fmt"
)

// User represents the user schema used in messages and comments.
type User struct {
	UserId int64  `json:"userId"`
	Name   string `json:"name"`
	Photo  []byte `json:"photo,omitempty"`
}

// CreateUser create a new user in the database
func (db *appdbimpl) CreateUser(name string) (User, error) {
	result, err := db.c.Exec("INSERT INTO users (name) VALUES (?)", name)
	if err != nil {
		return User{}, fmt.Errorf("error inserting user: %w", err)
	}

	userId, err := result.LastInsertId()
	if err != nil {
		return User{}, fmt.Errorf("error retrieving last insert id: %w", err)
	}

	return User{
		UserId: userId,
	}, nil
}

// GetUserByName search a user by name
func (db *appdbimpl) GetUserByName(name string) (*User, error) {
	var user User
	var photo []byte

	// Query to retrieve the user details
	err := db.c.QueryRow("SELECT id, name, photo FROM users WHERE name = ?", name).Scan(&user.UserId, &user.Name, &photo)
	if errors.Is(err, sql.ErrNoRows) {
		return nil, nil // User not found
	}
	if err != nil {
		return nil, err
	}

	// If photo is valid, assign the photo as a byte slice
	if len(photo) == 0 {
		user.Photo = nil
	} else {
		user.Photo = photo
	}

	return &user, nil
}

// UpdateUsername update the name of a specified user
func (db *appdbimpl) UpdateUsername(userId int64, newUsername string) error {
	_, err := db.c.Exec("UPDATE users SET name = ? WHERE id = ?", newUsername, userId)
	return err
}

// GetUserById retrieves a user from the database by their ID
func (db *appdbimpl) GetUserById(userId int64) (User, error) {
	var user User
	var photo []byte

	// Query to retrieve the user details
	err := db.c.QueryRow(`
		SELECT id, name, photo
		FROM users
		WHERE id = ?`, userId).Scan(&user.UserId, &user.Name, &photo)
	if err != nil {
		if errors.Is(err, sql.ErrNoRows) {
			return User{}, fmt.Errorf("user not found: %w", err)
		}
		return User{}, fmt.Errorf("error retrieving user: %w", err)
	}

	// If photo is valid, assign the photo as a byte slice
	if len(photo) == 0 {
		user.Photo = nil
	} else {
		user.Photo = photo
	}

	return user, nil
}

// IsUserInConversation checks if a user is part of a conversation.
func (db *appdbimpl) IsUserInConversation(userId int64, conversationId int64) (bool, error) {
	var exists bool
	err := db.c.QueryRow(`
		SELECT EXISTS (
			SELECT 1 
			FROM conversation_members 
			WHERE user_id = ? AND conversation_id = ?
		)`, userId, conversationId).Scan(&exists)
	if err != nil {
		return false, fmt.Errorf("error checking if user is in conversation: %w", err)
	}
	return exists, nil
}

// SearchUsersByUsername searches for users whose username contains the search string
func (db *appdbimpl) SearchUsersByUsername(username string) ([]User, error) {
	var query string
	var args []interface{}

	if username == "" {
		// No username provided, return all users
		query = `SELECT id, name, photo FROM users`
	} else {
		// Search for users whose name contains the search string
		query = `SELECT id, name, photo FROM users WHERE name LIKE ? LIMIT 10`
		args = append(args, "%"+username+"%")
	}

	rows, err := db.c.Query(query, args...)
	if err != nil {
		return nil, fmt.Errorf("error searching users by username: %w", err)
	}
	defer rows.Close()

	var users []User
	for rows.Next() {
		var user User
		var photo []byte

		if err := rows.Scan(&user.UserId, &user.Name, &photo); err != nil {
			return nil, fmt.Errorf("error scanning user row: %w", err)
		}

		// If photo is valid, assign it as a byte slice
		if len(photo) > 0 {
			user.Photo = photo
		} else {
			user.Photo = nil
		}

		users = append(users, user)
	}

	if err := rows.Err(); err != nil {
		return nil, fmt.Errorf("rows iteration error: %w", err)
	}

	return users, nil
}
