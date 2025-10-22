package api

import (
	"net/http"
	"strconv"

	"github.com/LorenzoSabatino/WASAText/service/api/reqcontext"
	"github.com/julienschmidt/httprouter"
)

// deleteMessage handles the API request.
func (rt *_router) deleteMessage(w http.ResponseWriter, r *http.Request, ps httprouter.Params, ctx reqcontext.RequestContext) {
	// Extract userId, conversationId, and messageId from path parameters
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

	// Verify if the message exists
	message, err := rt.db.GetMessageById(messageId, conversationId)
	if err != nil {
		ctx.Logger.WithError(err).Error("message not found")
		http.Error(w, "Message not found", http.StatusNotFound)
		return
	}

	// Verify ownership or permissions
	if message.Sender.UserId != userId {
		ctx.Logger.Error("user unauthorized to delete message")
		http.Error(w, "Unauthorized", http.StatusForbidden)
		return
	}

	// Delete the message
	err = rt.db.DeleteMessageById(messageId)
	if err != nil {
		ctx.Logger.WithError(err).Error("failed to delete message")
		http.Error(w, "Failed to delete message", http.StatusInternalServerError)
		return
	}

	// Respond with no content (204)
	w.WriteHeader(http.StatusNoContent)
}
