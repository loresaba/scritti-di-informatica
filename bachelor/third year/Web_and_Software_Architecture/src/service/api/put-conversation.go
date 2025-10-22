package api

import (
	"encoding/json"
	"net/http"
	"strconv"

	"github.com/LorenzoSabatino/WASAText/service/api/reqcontext"
	"github.com/julienschmidt/httprouter"
)

// addConversation handles starting a new conversation
func (rt *_router) addConversation(w http.ResponseWriter, r *http.Request, ps httprouter.Params, ctx reqcontext.RequestContext) {
	// Extract userId from path parameters
	userIdStr := ps.ByName("userId")
	userId, err := strconv.ParseInt(userIdStr, 10, 64)
	if err != nil || userId <= 0 {
		ctx.Logger.WithError(err).Error("invalid userId")
		http.Error(w, "Invalid user id", http.StatusBadRequest)
		return
	}

	// Check if the user exists
	user, err := rt.db.GetUserById(userId)
	if err != nil {
		ctx.Logger.WithError(err).Error("user not found")
		http.Error(w, "User not found", http.StatusNotFound)
		return
	}

	// Parse request body to get the details of the target user for the conversation
	var body struct {
		TargetUsername string `json:"targetUsername"`
		Type           string `json:"type"`
	}

	if err := json.NewDecoder(r.Body).Decode(&body); err != nil {
		ctx.Logger.WithError(err).Error("invalid request body")
		http.Error(w, "Invalid data", http.StatusBadRequest)
		return
	}

	// Check if the target user exists
	targetUser, err := rt.db.GetUserByName(body.TargetUsername)
	if err != nil {
		ctx.Logger.WithError(err).Error("target user not found")
		http.Error(w, "Target user not found", http.StatusNotFound)
		return
	}

	// Check if the type is valid (either "group" or "direct")
	if body.Type != "group" && body.Type != "direct" {
		ctx.Logger.WithError(err).Error("invalid conversation type")
		http.Error(w, "Invalid conversation type", http.StatusBadRequest)
		return
	}

	// Create a new conversation between the user and the target user
	conversation, err := rt.db.CreateConversation(user.UserId, targetUser.UserId, body.Type)
	if err != nil {
		ctx.Logger.WithError(err).Error("failed to create conversation")
		http.Error(w, "Failed to create conversation", http.StatusInternalServerError)
		return
	}

	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(http.StatusCreated)

	if err := json.NewEncoder(w).Encode(conversation); err != nil {
		ctx.Logger.WithError(err).Error("failed to write response")
		http.Error(w, "Failed to write response", http.StatusInternalServerError)
	}
}
