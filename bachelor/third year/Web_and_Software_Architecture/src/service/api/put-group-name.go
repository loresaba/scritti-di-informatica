package api

import (
	"encoding/json"
	"net/http"
	"regexp"
	"strconv"

	"github.com/LorenzoSabatino/WASAText/service/api/reqcontext"
	"github.com/julienschmidt/httprouter"
)

// setGroupName handles the API request
func (rt *_router) setGroupName(w http.ResponseWriter, r *http.Request, ps httprouter.Params, ctx reqcontext.RequestContext) {
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

	// Parse request body
	var requestBody struct {
		Name string `json:"name"`
	}
	if err := json.NewDecoder(r.Body).Decode(&requestBody); err != nil {
		ctx.Logger.WithError(err).Error("invalid request body")
		http.Error(w, "Invalid request body", http.StatusBadRequest)
		return
	}

	// Validate the group name
	if len(requestBody.Name) < 1 || len(requestBody.Name) > 32 || !isValidGroupName(requestBody.Name) {
		http.Error(w, "Invalid group name", http.StatusBadRequest)
		return
	}

	// Check if the user exists
	_, err = rt.db.GetUserById(userId)
	if err != nil {
		ctx.Logger.WithError(err).Error("user not found")
		http.Error(w, "User not found", http.StatusNotFound)
		return
	}

	// Check if the group exists
	group, err := rt.db.GetGroupById(int64(groupId))
	if err != nil {
		ctx.Logger.WithError(err).Error("group not found")
		http.Error(w, "Group not found", http.StatusNotFound)
		return
	}

	// Check if the user is a member of the group
	isMember, err := rt.db.IsUserMemberOfGroup(userId, int64(groupId))
	if err != nil {
		ctx.Logger.WithError(err).Error("database error checking group membership")
		http.Error(w, "Database error", http.StatusInternalServerError)
		return
	}
	if !isMember {
		ctx.Logger.Error("user is not a member of the group")
		http.Error(w, "User is not a member of the group", http.StatusNotFound)
		return
	}

	// Update the group name
	err = rt.db.UpdateGroupName(int64(groupId), requestBody.Name)
	if err != nil {
		ctx.Logger.WithError(err).Error("failed to update group name")
		http.Error(w, "Failed to update group name", http.StatusInternalServerError)
		return
	}

	// Respond with success
	response := struct {
		GroupId int64  `json:"groupId"`
		NewName string `json:"newName"`
	}{
		GroupId: group.ConversationId,
		NewName: requestBody.Name,
	}
	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(http.StatusOK)
	if err := json.NewEncoder(w).Encode(response); err != nil {
		ctx.Logger.WithError(err).Error("failed to encode response")
		http.Error(w, "Failed to encode response", http.StatusInternalServerError)
	}
}

// isValidGroupName validates the group name (only allowing alphanumeric characters and spaces)
func isValidGroupName(name string) bool {
	// Regular expression to match valid group names
	re := regexp.MustCompile(`^[a-zA-Z0-9 ]+$`)
	return re.MatchString(name)
}
