package api

import (
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

// User represents the user schema used in messages and comments.
type User struct {
	UserId int64  `json:"userId"`
	Name   string `json:"name"`
	Photo  []byte `json:"photo,omitempty"`
}

// Group represents a single group.
type Group struct {
	GroupId int64  `json:"groupId"`
	Name    string `json:"name"`
	Photo   []byte `json:"photo,omitempty"`
}
