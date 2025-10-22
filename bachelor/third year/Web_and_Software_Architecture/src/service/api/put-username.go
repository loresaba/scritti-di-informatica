package api

import (
	"encoding/json"
	"net/http"
	"strconv"

	"github.com/LorenzoSabatino/WASAText/service/api/reqcontext"
	"github.com/julienschmidt/httprouter"
)

// UsernameRequestBody represents the payload received in the request
type UsernameRequestBody struct {
	Username string `json:"username"`
}

// UsernameResponse represents the response returned by the endpoint
type UsernameResponse struct {
	UserID      int64  `json:"userId"`
	NewUsername string `json:"newUsername"`
}

// putUsername handles the API request
func (rt *_router) putUsername(w http.ResponseWriter, r *http.Request, ps httprouter.Params, ctx reqcontext.RequestContext) {
	// Extract userId from the path parameters
	userIdStr := ps.ByName("userId")
	userId, errUsr := strconv.ParseInt(userIdStr, 10, 64)
	if errUsr != nil || userId <= 0 {
		ctx.Logger.WithError(errUsr).Error("invalid userId")
		http.Error(w, "Invalid user id", http.StatusBadRequest)
		return
	}

	// Parse the request body
	var body UsernameRequestBody
	if err := json.NewDecoder(r.Body).Decode(&body); err != nil {
		ctx.Logger.WithError(err).Error("invalid request body")
		http.Error(w, "Invalid request body", http.StatusBadRequest)
		return
	}

	// Validate the new username
	if err := validateName(body.Username); err != nil {
		ctx.Logger.WithError(err).Error("invalid username")
		http.Error(w, "Invalid username", http.StatusBadRequest)
		return
	}

	// Verify if the user name altready exists
	existingUser, err := rt.db.GetUserByName(body.Username)
	if err != nil {
		ctx.Logger.WithError(err).Error("failed to check username")
		http.Error(w, "Internal Server Error", http.StatusInternalServerError)
		return
	}

	// If the user exists, return an error
	if existingUser != nil {
		ctx.Logger.Error("username already in use")
		http.Error(w, "Username already in use", http.StatusBadRequest)
		return
	}

	// Update the name of the user in the database
	err = rt.db.UpdateUsername(userId, body.Username)
	if err != nil {
		ctx.Logger.WithError(err).Error("failed to update username")
		http.Error(w, "Internal Server Error", http.StatusInternalServerError)
		return
	}

	// Build the response
	response := UsernameResponse{
		UserID:      userId,
		NewUsername: body.Username,
	}

	w.Header().Set("content-type", "application/json")
	w.WriteHeader(http.StatusOK)
	if err := json.NewEncoder(w).Encode(response); err != nil {
		ctx.Logger.WithError(err).Error("failed to write response")
		http.Error(w, "Internal Server Error", http.StatusInternalServerError)
	}
}
