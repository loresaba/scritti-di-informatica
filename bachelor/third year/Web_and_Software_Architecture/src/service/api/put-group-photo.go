package api

import (
	"encoding/json"
	"io/ioutil"
	"net/http"
	"strconv"

	"github.com/LorenzoSabatino/WASAText/service/api/reqcontext"
	"github.com/julienschmidt/httprouter"
)

// setGroupPhoto handles the API request to upload and set a new group photo
func (rt *_router) setGroupPhoto(w http.ResponseWriter, r *http.Request, ps httprouter.Params, ctx reqcontext.RequestContext) {
	// Extract userId and groupId from the path parameters
	userIdStr := ps.ByName("userId")
	userId, errUsr := strconv.ParseInt(userIdStr, 10, 64)
	groupIdStr := ps.ByName("groupId")
	groupId, err := strconv.ParseInt(groupIdStr, 10, 64)
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

	// Parse the multipart/form-data request to get the photo file
	err = r.ParseMultipartForm(10 << 20) // Limit file size to 10 MB
	if err != nil {
		ctx.Logger.WithError(err).Error("failed to parse multipart form")
		http.Error(w, "Failed to parse multipart form", http.StatusBadRequest)
		return
	}

	// Get the photo file from the form
	file, _, err := r.FormFile("photo")
	if err != nil {
		ctx.Logger.WithError(err).Error("failed to get photo from form")
		http.Error(w, "Failed to get photo", http.StatusBadRequest)
		return
	}
	defer file.Close()

	// Read the photo file into a byte slice
	photoData, err := ioutil.ReadAll(file)
	if err != nil {
		ctx.Logger.WithError(err).Error("failed to read photo file")
		http.Error(w, "Failed to read photo", http.StatusInternalServerError)
		return
	}

	// Update the group's photo in the database
	err = rt.db.UpdateGroupPhoto(group.ConversationId, photoData)
	if err != nil {
		ctx.Logger.WithError(err).Error("failed to update group photo")
		http.Error(w, "Failed to update group photo", http.StatusInternalServerError)
		return
	}

	// Respond with success
	response := struct {
		GroupId int64  `json:"groupId"`
		Photo   []byte `json:"photo,omitempty"`
	}{
		GroupId: groupId,
		Photo:   photoData,
	}
	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(http.StatusOK)
	if err := json.NewEncoder(w).Encode(response); err != nil {
		ctx.Logger.WithError(err).Error("failed to encode response")
		http.Error(w, "Failed to encode response", http.StatusInternalServerError)
	}
}
