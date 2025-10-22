package api

import (
	"encoding/json"
	"net/http"
	"strconv"
	"unicode/utf8"

	"github.com/LorenzoSabatino/WASAText/service/api/reqcontext"
	"github.com/julienschmidt/httprouter"
)

// CommentRequest represents the request payload for adding a comment to a message
type CommentRequest struct {
	Content string `json:"content"`
}

// commentMessage handles the API request
func (rt *_router) commentMessage(w http.ResponseWriter, r *http.Request, ps httprouter.Params, ctx reqcontext.RequestContext) {
	// Extract userId, conversationId and messageId from path parameters
	userIdStr := ps.ByName("userId")
	userId, errUsr := strconv.ParseInt(userIdStr, 10, 64)
	conversationIdStr := ps.ByName("conversationId")
	messageIdStr := ps.ByName("messageId")
	conversationId, err := strconv.ParseInt(conversationIdStr, 10, 64)
	messageId, errMsg := strconv.ParseInt(messageIdStr, 10, 64)
	if err != nil || errUsr != nil || errMsg != nil || userId <= 0 || conversationId <= 0 || messageId <= 0 {
		ctx.Logger.WithError(err).Error("invalid userId, conversationId or messageId")
		http.Error(w, "Invalid user, conversation or message id", http.StatusBadRequest)
		return
	}

	// Check if the user exists
	_, err = rt.db.GetUserById(userId)
	if err != nil {
		ctx.Logger.WithError(err).Error("user not found")
		http.Error(w, "User not found", http.StatusNotFound)
		return
	}

	// Check if the user is part of the conversation
	isMember, err := rt.db.IsUserInConversation(userId, conversationId)
	if err != nil {
		ctx.Logger.WithError(err).Error("error checking if user is in conversation")
		http.Error(w, "Internal Server Error", http.StatusInternalServerError)
		return
	}
	if !isMember {
		ctx.Logger.Error("user is not a member of the conversation")
		http.Error(w, "Forbidden", http.StatusForbidden)
		return
	}

	// Decode the request body
	var commentRequest CommentRequest
	if err := json.NewDecoder(r.Body).Decode(&commentRequest); err != nil {
		ctx.Logger.WithError(err).Error("failed to parse request body")
		http.Error(w, "Invalid request body", http.StatusBadRequest)
		return
	}

	// Validate the content of the comment (emoji reaction)
	if !isValidEmoji(commentRequest.Content) {
		ctx.Logger.Error("invalid comment format")
		http.Error(w, "Invalid comment format", http.StatusBadRequest)
		return
	}

	// Check if the message is in the conversation
	_, err = rt.db.GetMessageById(messageId, conversationId)
	if err != nil {
		ctx.Logger.WithError(err).Error("message not found")
		http.Error(w, "Message not found", http.StatusNotFound)
		return
	}

	// Add the comment to the database
	newComment, err := rt.db.AddCommentToMessage(messageId, userId, commentRequest.Content)
	if err != nil {
		ctx.Logger.WithError(err).Error("failed to add comment")
		http.Error(w, "Failed to add comment", http.StatusInternalServerError)
		return
	}

	// Respond with the added comment
	w.Header().Set("content-type", "application/json")
	w.WriteHeader(http.StatusOK)
	if err := json.NewEncoder(w).Encode(newComment); err != nil {
		ctx.Logger.WithError(err).Error("failed to encode response")
		http.Error(w, "Internal Server Error", http.StatusInternalServerError)
	}
}

// isValidEmoji checks if the content is a valid emoji reaction
func isValidEmoji(emoji string) bool {
	r, _ := utf8.DecodeRuneInString(emoji)
	if (r >= '\U0001F600' && r <= '\U0001F64F') ||
		(r >= '\U0001F300' && r <= '\U0001F5FF') ||
		(r >= '\U0001F680' && r <= '\U0001F6FF') ||
		(r >= '\U0001F700' && r <= '\U0001F77F') {
		return true
	}
	return false
}
