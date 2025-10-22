package api

import (
	"encoding/json"
	"net/http"
	"strconv"

	"github.com/LorenzoSabatino/WASAText/service/api/reqcontext"
	"github.com/LorenzoSabatino/WASAText/service/database"
	"github.com/julienschmidt/httprouter"
)

// getConversation handles the API request.
func (rt *_router) getConversation(w http.ResponseWriter, r *http.Request, ps httprouter.Params, ctx reqcontext.RequestContext) {
	// Extract userId and conversationId from path parameters
	userIdStr := ps.ByName("userId")
	userId, errUsr := strconv.ParseInt(userIdStr, 10, 64)
	conversationIdStr := ps.ByName("conversationId")
	conversationId, err := strconv.ParseInt(conversationIdStr, 10, 64)
	if err != nil || errUsr != nil || userId <= 0 || conversationId <= 0 {
		ctx.Logger.WithError(err).Error("invalid userId or conversationId")
		http.Error(w, "Invalid user or conversation id", http.StatusBadRequest)
		return
	}

	// Extract and validate the "sort" query parameter
	sortOrder := r.URL.Query().Get("sort")
	sortOrder, err = validateSort(sortOrder)
	if err != nil {
		ctx.Logger.WithError(err).Error("invalid sort parameter")
		http.Error(w, "Invalid sort parameter", http.StatusBadRequest)
		return
	}

	// Check if the user exists
	_, err = rt.db.GetUserById(userId)
	if err != nil {
		ctx.Logger.WithError(err).Error("user not found")
		http.Error(w, "User not found", http.StatusNotFound)
		return
	}

	// Fetch messages from the database
	messages, err := rt.db.GetMessagesByConversation(userId, conversationId, sortOrder)
	if err != nil {
		ctx.Logger.WithError(err).Error("failed to fetch messages")
		http.Error(w, "Internal Server Error", http.StatusInternalServerError)
		return
	}

	// Response payload
	response := struct {
		ConversationID int64              `json:"conversationId"`
		Messages       []database.Message `json:"messages"`
	}{
		ConversationID: conversationId,
		Messages:       messages,
	}

	// Respond with the conversation details
	w.Header().Set("content-type", "application/json")
	if err := json.NewEncoder(w).Encode(response); err != nil {
		ctx.Logger.WithError(err).Error("failed to encode response")
		http.Error(w, "Internal Server Error", http.StatusInternalServerError)
	}
}
