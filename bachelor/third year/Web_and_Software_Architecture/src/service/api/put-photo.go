package api

import (
	"encoding/json"
	"io/ioutil"
	"net/http"
	"strconv"

	"github.com/LorenzoSabatino/WASAText/service/api/reqcontext"
	"github.com/julienschmidt/httprouter"
)

// setMyPhoto handles the API request to upload and set a new user photo
func (rt *_router) setMyPhoto(w http.ResponseWriter, r *http.Request, ps httprouter.Params, ctx reqcontext.RequestContext) {
	// Extract userId from the path parameters
	userIdStr := ps.ByName("userId")
	userId, errUsr := strconv.ParseInt(userIdStr, 10, 64)

	if errUsr != nil || userId <= 0 {
		ctx.Logger.WithError(errUsr).Error("invalid userId")
		http.Error(w, "Invalid user", http.StatusBadRequest)
		return
	}

	// Check if the user exists
	user, err := rt.db.GetUserById(userId)
	if err != nil {
		ctx.Logger.WithError(err).Error("user not found")
		http.Error(w, "User not found", http.StatusNotFound)
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

	// Update the user's photo in the database
	err = rt.db.UpdateUserPhoto(user.UserId, photoData)
	if err != nil {
		ctx.Logger.WithError(err).Error("failed to update photo")
		http.Error(w, "Failed to update photo", http.StatusInternalServerError)
		return
	}

	// Respond with success
	response := struct {
		UserId int64  `json:"userId"`
		Photo  []byte `json:"photo,omitempty"`
	}{
		UserId: userId,
		Photo:  photoData,
	}
	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(http.StatusOK)
	if err := json.NewEncoder(w).Encode(response); err != nil {
		ctx.Logger.WithError(err).Error("failed to encode response")
		http.Error(w, "Failed to encode response", http.StatusInternalServerError)
	}
}
