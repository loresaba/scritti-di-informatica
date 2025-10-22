package api

import (
	"encoding/json"
	"errors"
	"net/http"
	"regexp"

	"github.com/LorenzoSabatino/WASAText/service/api/reqcontext"
	"github.com/julienschmidt/httprouter"
)

// UserRequestBody represents the payload received in the request
type UserRequestBody struct {
	Name string `json:"name"`
}

// UserResponse represents the response returned by the endpoint
type UserResponse struct {
	Id int64 `json:"id"`
}

// validateName ensures the name satisfies the required length and pattern
func validateName(name string) error {
	if len(name) < 3 || len(name) > 16 {
		return errors.New("name must be between 3 and 16 characters")
	}
	pattern := `^.*?$`
	matched, err := regexp.MatchString(pattern, name)
	if err != nil || !matched {
		return errors.New("name does not match the required pattern")
	}
	return nil
}

// postSession handles the API request
func (rt *_router) postSession(w http.ResponseWriter, r *http.Request, ps httprouter.Params, ctx reqcontext.RequestContext) {
	var body UserRequestBody
	if err := json.NewDecoder(r.Body).Decode(&body); err != nil {
		ctx.Logger.WithError(err).Error("invalid request body")
		http.Error(w, "Invalid request body", http.StatusBadRequest)
		return
	}

	// Validate the user name
	if err := validateName(body.Name); err != nil {
		ctx.Logger.WithError(err).Error("invalid name in request body")
		http.Error(w, "Invalid name", http.StatusBadRequest)
		return
	}

	// Verify if the user name altready exists
	existingUser, err := rt.db.GetUserByName(body.Name)
	if err != nil {
		ctx.Logger.WithError(err).Error("failed to query user")
		http.Error(w, "Internal Server Error", http.StatusInternalServerError)
		return
	}

	// If the user exists, return its id
	if existingUser != nil {
		response := UserResponse{
			Id: existingUser.UserId,
		}
		w.Header().Set("content-type", "application/json")
		w.WriteHeader(http.StatusOK)
		if err := json.NewEncoder(w).Encode(response); err != nil {
			ctx.Logger.WithError(err).Error("failed to write response")
			http.Error(w, "Internal Server Error", http.StatusInternalServerError)
		}
		return
	}

	// Create the new user in the database
	newUser, err := rt.db.CreateUser(body.Name)
	if err != nil {
		ctx.Logger.WithError(err).Error("failed to create user")
		http.Error(w, "Internal Server Error", http.StatusInternalServerError)
		return
	}

	// Return the new user's ID
	response := UserResponse{
		Id: newUser.UserId,
	}

	w.Header().Set("content-type", "application/json")
	w.WriteHeader(http.StatusCreated)
	if err := json.NewEncoder(w).Encode(response); err != nil {
		ctx.Logger.WithError(err).Error("failed to write response")
		http.Error(w, "Internal Server Error", http.StatusInternalServerError)
	}
}
