package api

import (
	"net/http"
	"strconv"

	"github.com/LorenzoSabatino/WASAText/service/api/reqcontext"
	"github.com/julienschmidt/httprouter"
)

// leaveGroup handles the API request
func (rt *_router) leaveGroup(w http.ResponseWriter, r *http.Request, ps httprouter.Params, ctx reqcontext.RequestContext) {
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

	// Check if the group exists
	_, err = rt.db.GetGroupById(int64(groupId))
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

	// Remove the user from the group
	err = rt.db.RemoveUserFromGroup(int64(groupId), userId)
	if err != nil {
		ctx.Logger.WithError(err).Error("failed to remove user from group")
		http.Error(w, "Failed to remove user from group", http.StatusInternalServerError)
		return
	}

	// Respond with success
	w.WriteHeader(http.StatusNoContent)
}
