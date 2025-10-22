package api

import (
	"encoding/json"
	"net/http"
	"strconv"

	"github.com/LorenzoSabatino/WASAText/service/api/reqcontext"
	"github.com/julienschmidt/httprouter"
)

func (rt *_router) searchUsers(w http.ResponseWriter, r *http.Request, ps httprouter.Params, ctx reqcontext.RequestContext) {
	// Extract the userId from the path parameters
	userIdStr := ps.ByName("userId")
	userId, errUsr := strconv.ParseInt(userIdStr, 10, 64)
	if errUsr != nil || userId <= 0 {
		ctx.Logger.WithError(errUsr).Error("invalid userId")
		http.Error(w, "Invalid userId", http.StatusBadRequest)
		return
	}

	// Extract and validate the "username" query parameter
	username := r.URL.Query().Get("username")

	// Check if the user exists
	_, err := rt.db.GetUserById(userId)
	if err != nil {
		ctx.Logger.WithError(err).Error("user not found")
		http.Error(w, "User not found", http.StatusNotFound)
		return
	}

	// Perform the search in the database for the username
	users, err := rt.db.SearchUsersByUsername(username)
	if err != nil {
		ctx.Logger.WithError(err).Error("failed to search users in database")
		http.Error(w, "Internal Server Error", http.StatusInternalServerError)
		return
	}

	// Return the list of matching users
	w.Header().Set("Content-Type", "application/json")
	if err := json.NewEncoder(w).Encode(users); err != nil {
		ctx.Logger.WithError(err).Error("failed to encode response")
		http.Error(w, "Internal Server Error", http.StatusInternalServerError)
	}
}
