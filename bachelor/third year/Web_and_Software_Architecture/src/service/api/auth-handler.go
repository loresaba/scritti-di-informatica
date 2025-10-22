package api

import (
	"net/http"
	"strconv"

	"github.com/LorenzoSabatino/WASAText/service/api/reqcontext"
	"github.com/julienschmidt/httprouter"
)

func (rt *_router) AuthHandler(next httpRouterHandler) httpRouterHandler {
	return func(w http.ResponseWriter, r *http.Request, ps httprouter.Params, ctx reqcontext.RequestContext) {
		// Get Authorization header
		authHeader := r.Header.Get("Authorization")
		if authHeader == "" {
			ctx.Logger.Warn("missing Authorization header")
			http.Error(w, "Unauthorized: missing Authorization header", http.StatusUnauthorized)
			return
		}

		// Validate user ID from Authorization header
		userIDStr := authHeader
		userID, err := strconv.ParseInt(userIDStr, 10, 64)
		if err != nil {
			ctx.Logger.Warn("invalid user ID format")
			http.Error(w, "Unauthorized: invalid user ID format", http.StatusUnauthorized)
			return
		}

		// Check if the user is in the database
		_, err = rt.db.GetUserById(userID)
		if err != nil {
			ctx.Logger.Warn("invalid Authorization header")
			http.Error(w, "Unauthorized: invalid user ID", http.StatusUnauthorized)
			return
		}

		// Proceed to the next handler
		next(w, r, ps, ctx)
	}
}
