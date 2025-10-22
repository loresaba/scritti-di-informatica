package api

import (
	"encoding/json"
	"net/http"
	"strconv"

	"github.com/LorenzoSabatino/WASAText/service/api/reqcontext"
	"github.com/julienschmidt/httprouter"
)

func (rt *_router) getGroupMembers(w http.ResponseWriter, r *http.Request, ps httprouter.Params, ctx reqcontext.RequestContext) {
	// Extract the userId and groupId from the path parameters
	userIdStr := ps.ByName("userId")
	groupIdStr := ps.ByName("groupId")

	userId, err := strconv.ParseInt(userIdStr, 10, 64)
	if err != nil || userId <= 0 {
		http.Error(w, "Invalid userId", http.StatusBadRequest)
		return
	}

	groupId, err := strconv.ParseInt(groupIdStr, 10, 64)
	if err != nil || groupId <= 0 {
		http.Error(w, "Invalid groupId", http.StatusBadRequest)
		return
	}

	// Check if the user is a member of the group
	isMember, err := rt.db.IsUserMemberOfGroup(userId, groupId)
	if err != nil {
		http.Error(w, "Failed to verify group membership", http.StatusInternalServerError)
		return
	}
	if !isMember {
		http.Error(w, "User is not a member of the group", http.StatusForbidden)
		return
	}

	// Retrieve group members from database
	members, err := rt.db.GetGroupMembers(groupId)
	if err != nil {
		http.Error(w, "Failed to retrieve group members", http.StatusInternalServerError)
		return
	}

	// Converts the member list to JSON and sends the response
	w.Header().Set("Content-Type", "application/json")
	if err := json.NewEncoder(w).Encode(members); err != nil {
		http.Error(w, "Failed to encode response", http.StatusInternalServerError)
		return
	}
}
