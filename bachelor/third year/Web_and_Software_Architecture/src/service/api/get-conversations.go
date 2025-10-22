package api

import (
	"encoding/json"
	"errors"
	"net/http"
	"strconv"

	"github.com/LorenzoSabatino/WASAText/service/api/reqcontext"
	"github.com/julienschmidt/httprouter"
)

// validateSort validates the sorting parameter.
func validateSort(sortOrder string) (string, error) {
	if sortOrder == "asc" || sortOrder == "desc" {
		return sortOrder, nil
	}
	return "", errors.New("invalid sort parameter; use 'asc' or 'desc'")
}

// getConversations handles the API request
func (rt *_router) getConversations(w http.ResponseWriter, r *http.Request, ps httprouter.Params, ctx reqcontext.RequestContext) {
	// Extract userId from the path parameters
	userIdStr := ps.ByName("userId")
	userId, errUsr := strconv.ParseInt(userIdStr, 10, 64)
	if errUsr != nil || userId <= 0 {
		ctx.Logger.WithError(errUsr).Error("invalid userId")
		http.Error(w, "Invalid userId", http.StatusBadRequest)
		return
	}

	// Extract and validate the "sort" query parameter
	sortOrder := r.URL.Query().Get("sort")
	sortOrder, err := validateSort(sortOrder)
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

	// Get the conversation from the database
	conversations, err := rt.db.GetConversationsByUser(userId, sortOrder)
	if err != nil {
		ctx.Logger.WithError(err).Error("failed to fetch conversations from database")
		http.Error(w, "Internal Server Error", http.StatusInternalServerError)
		return
	}

	// Respond with the list of conversations
	w.Header().Set("content-type", "application/json")
	if err := json.NewEncoder(w).Encode(conversations); err != nil {
		ctx.Logger.WithError(err).Error("failed to encode response")
		http.Error(w, "Internal Server Error", http.StatusInternalServerError)
	}
}
