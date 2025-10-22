/*
Package database is the middleware between the app database and the code. All data (de)serialization (save/load) from a
persistent database are handled here. Database specific logic should never escape this package.

To use this package you need to apply migrations to the database if needed/wanted, connect to it (using the database
data source name from config), and then initialize an instance of AppDatabase from the DB connection.

For example, this code adds a parameter in `webapi` executable for the database data source name (add it to the
main.WebAPIConfiguration structure):

	DB struct {
		Filename string `conf:""`
	}

This is an example on how to migrate the DB and connect to it:

	// Start Database
	logger.Println("initializing database support")
	db, err := sql.Open("sqlite3", "./foo.db")
	if err != nil {
		logger.WithError(err).Error("error opening SQLite DB")
		return fmt.Errorf("opening SQLite: %w", err)
	}
	defer func() {
		logger.Debug("database stopping")
		_ = db.Close()
	}()

Then you can initialize the AppDatabase and pass it to the api package.
*/
package database

import (
	"database/sql"
	"errors"
	"fmt"
)

// AppDatabase is the high level interface for the DB
type AppDatabase interface {
	GetName() (string, error)
	SetName(name string) error

	Ping() error

	CreateUser(name string) (User, error)
	GetUserByName(name string) (*User, error)
	UpdateUsername(userId int64, newUsername string) error
	GetUserById(userId int64) (User, error)
	IsUserInConversation(userId int64, conversationId int64) (bool, error)
	GetConversationsByUser(userId int64, sortOrder string) ([]Conversation, error)
	GetMessagesByConversation(userId int64, conversationId int64, sortOrder string) ([]Message, error)
	GetCommentsByMessage(messageId int64) ([]Comment, error)
	AddMessage(conversationId int64, senderId int64, content string, status string, messageType string, photo []byte) (Message, error)
	ForwardMessage(userId int64, originalMessage Message, targetConversationId int64) (Message, error)
	GetMessageById(messageId int64, conversationId int64) (Message, error)
	AddCommentToMessage(messageId int64, senderId int64, content string) (Comment, error)
	GetCommentById(commentId int64) (Comment, error)
	DeleteCommentById(commentId int64) error
	DeleteMessageById(messageId int64) error
	GetGroupById(conversationId int64) (*Conversation, error)
	AddUserToGroup(conversationId int64, userId int64) error
	IsUserMemberOfGroup(userId int64, conversationId int64) (bool, error)
	RemoveUserFromGroup(conversationId int64, userId int64) error
	UpdateGroupName(conversationId int64, newName string) error
	UpdateUserPhoto(userId int64, photoData []byte) error
	UpdateGroupPhoto(conversationId int64, photoData []byte) error
	GetConversationById(conversationId int64) (Conversation, error)
	CreateConversation(user1Id, user2Id int64, conversationType string) (Conversation, error)
	SearchUsersByUsername(username string) ([]User, error)
	GetGroupMembers(groupId int64) ([]string, error)
	ReplyMessage(conversationId int64, senderId int64, replyMessageId int64, text string, status string, messageType string, photo []byte) (Message, error)
}

type appdbimpl struct {
	c *sql.DB
}

// New returns a new instance of AppDatabase based on the SQLite connection `db`.
// `db` is required - an error will be returned if `db` is `nil`.
func New(db *sql.DB) (AppDatabase, error) {
	if db == nil {
		return nil, errors.New("database is required when building a AppDatabase")
	}

	// Check if table exists. If not, the database is empty, and we need to create the structure
	var tableName string
	err := db.QueryRow(`SELECT name FROM sqlite_master WHERE type='table' AND name='example_table';`).Scan(&tableName)
	if errors.Is(err, sql.ErrNoRows) {
		sqlStmt := `CREATE TABLE example_table (id INTEGER NOT NULL PRIMARY KEY, name TEXT);`
		_, err = db.Exec(sqlStmt)
		if err != nil {
			return nil, fmt.Errorf("error creating database structure: %w", err)
		}
	}

	// Create the users table if it doesn't already exist.
	if _, err := db.Exec(`
	CREATE TABLE IF NOT EXISTS users (
		id INTEGER PRIMARY KEY,
		name TEXT NOT NULL UNIQUE,
		photo BLOB
	)`); err != nil {
		return nil, fmt.Errorf("error creating users table: %w", err)
	}

	// Create the conversations table if it doesn't already exist.
	if _, err := db.Exec(`
		CREATE TABLE IF NOT EXISTS conversations (
			conversation_id INTEGER PRIMARY KEY,
			name TEXT NOT NULL,
			photo BLOB,
			last_message_id INTEGER,
			type TEXT CHECK(type IN ('group', 'direct')) NOT NULL,
			FOREIGN KEY (last_message_id) REFERENCES messages (message_id)
		)`); err != nil {
		return nil, fmt.Errorf("error creating conversations table: %w", err)
	}

	// Create the conversation members table if it doesn't already exist.
	if _, err := db.Exec(`
		CREATE TABLE IF NOT EXISTS conversation_members (
			conversation_id INTEGER NOT NULL,
			user_id INTEGER NOT NULL,
			last_access DATETIME,
			PRIMARY KEY (conversation_id, user_id),
			FOREIGN KEY (conversation_id) REFERENCES conversations (conversation_id) ON DELETE CASCADE,
			FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
		)`); err != nil {
		return nil, fmt.Errorf("error creating conversation_members table: %w", err)
	}

	// Create the messages table if it doesn't already exist.
	if _, err := db.Exec(`
		CREATE TABLE IF NOT EXISTS messages (
			message_id INTEGER PRIMARY KEY,
			timestamp DATETIME NOT NULL,
			text TEXT,
			photo BLOB,
			conversation_id INTEGER NOT NULL,
			sender_id INTEGER NOT NULL,
			status TEXT NOT NULL,
			type TEXT NOT NULL,
			reply_to_message_id INTEGER,
			FOREIGN KEY (conversation_id) REFERENCES conversations (conversation_id),
			FOREIGN KEY (sender_id) REFERENCES users (id)
			FOREIGN KEY (reply_to_message_id) REFERENCES messages (message_id)
		)`); err != nil {
		return nil, fmt.Errorf("error creating messages table: %w", err)
	}

	// Create the comments table if it doesn't already exist.
	if _, err := db.Exec(`
		CREATE TABLE IF NOT EXISTS comments (
			comment_id INTEGER PRIMARY KEY,
			message_id INTEGER NOT NULL,
			sender_id INTEGER NOT NULL,
			content TEXT NOT NULL,
			FOREIGN KEY (message_id) REFERENCES messages (message_id),
			FOREIGN KEY (sender_id) REFERENCES users (id)
		)`); err != nil {
		return nil, fmt.Errorf("error creating comments table: %w", err)
	}

	return &appdbimpl{
		c: db,
	}, nil
}

func (db *appdbimpl) Ping() error {
	return db.c.Ping()
}
