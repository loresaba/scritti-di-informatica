package api

import (
	"encoding/json"
	"net/http"
	"strconv"

	"github.com/LorenzoSabatino/WASAText/service/api/reqcontext"
	"github.com/julienschmidt/httprouter"
)

// addToGroup handles the API request.
func (rt *_router) addToGroup(w http.ResponseWriter, r *http.Request, ps httprouter.Params, ctx reqcontext.RequestContext) {
	// Extract userId and groupId from path parameters
	userIdStr := ps.ByName("userId")
	userId, errUsr := strconv.ParseInt(userIdStr, 10, 64)
	groupIdStr := ps.ByName("groupId")
	groupId, err := strconv.Atoi(groupIdStr)
	if err != nil || errUsr != nil || userId <= 0 || groupId <= 0 {
		ctx.Logger.WithError(err).Error("invalid userId or groupId")
		http.Error(w, "Invalid user or group id", http.StatusBadRequest)
		return
	}

	// Check if the user exists
	_, err = rt.db.GetUserById(userId)
	if err != nil {
		ctx.Logger.WithError(err).Error("user not found")
		http.Error(w, "User not found", http.StatusNotFound)
		return
	}

	// Parse request body to get the username to be added
	var body struct {
		Username string `json:"username"`
	}

	if err := json.NewDecoder(r.Body).Decode(&body); err != nil {
		ctx.Logger.WithError(err).Error("invalid request body")
		http.Error(w, "Invalid user data", http.StatusBadRequest)
		return
	}

	// Check if the user exists
	existingUser, err := rt.db.GetUserByName(body.Username)
	if err != nil {
		ctx.Logger.WithError(err).Error("user not found")
		http.Error(w, "User not found", http.StatusNotFound)
		return
	}

	// Check if the group exists
	_, err = rt.db.GetGroupById(int64(groupId))
	if err != nil {
		ctx.Logger.WithError(err).Error("group not found")
		http.Error(w, "Group not found", http.StatusNotFound)
		return
	}

	// Add user to group
	if err := rt.db.AddUserToGroup(int64(groupId), existingUser.UserId); err != nil {
		ctx.Logger.WithError(err).Error("failed to add user to group")
		http.Error(w, "Failed to add user to group", http.StatusInternalServerError)
		return
	}

	// Respond with success
	response := map[string]interface{}{
		"userId":  existingUser.UserId,
		"groupId": groupId,
	}
	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(http.StatusCreated)

	if err := json.NewEncoder(w).Encode(response); err != nil {
		ctx.Logger.WithError(err).Error("failed to write response")
		http.Error(w, "Failed to write response", http.StatusInternalServerError)
	}
}
