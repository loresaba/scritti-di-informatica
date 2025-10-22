package api

import (
	"encoding/json"
	"net/http"
	"strconv"

	"github.com/LorenzoSabatino/WASAText/service/api/reqcontext"
	"github.com/julienschmidt/httprouter"
)

// ForwardMessageRequest represents the request payload for forwarding a message
type ForwardMessageRequest struct {
	ConversationId int64 `json:"conversationId"`
}

// forwardMessage handles the API request
func (rt *_router) forwardMessage(w http.ResponseWriter, r *http.Request, ps httprouter.Params, ctx reqcontext.RequestContext) {
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
	var forwardMessageRequest ForwardMessageRequest
	if err := json.NewDecoder(r.Body).Decode(&forwardMessageRequest); err != nil {
		ctx.Logger.WithError(err).Error("failed to parse request body")
		http.Error(w, "Invalid request body", http.StatusBadRequest)
		return
	}

	// Retrieve the original message from the database
	originalMessage, err := rt.db.GetMessageById(messageId, conversationId)
	if err != nil {
		ctx.Logger.WithError(err).Error("message not found")
		http.Error(w, "Original message not found", http.StatusNotFound)
		return
	}

	// Check if the user is part of the target conversation
	isMember, err = rt.db.IsUserInConversation(userId, forwardMessageRequest.ConversationId)
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

	// Forward the message
	forwardedMessage, err := rt.db.ForwardMessage(userId, originalMessage, forwardMessageRequest.ConversationId)
	if err != nil {
		ctx.Logger.WithError(err).Error("failed to forward message")
		http.Error(w, "Failed to forward message", http.StatusInternalServerError)
		return
	}

	// Respond with the forwarded message
	w.Header().Set("content-type", "application/json")
	w.WriteHeader(http.StatusCreated)
	if err := json.NewEncoder(w).Encode(forwardedMessage); err != nil {
		ctx.Logger.WithError(err).Error("failed to encode response")
		http.Error(w, "Internal Server Error", http.StatusInternalServerError)
	}
}
