package api

import (
	"encoding/json"
	"io"
	"net/http"
	"strconv"

	"github.com/LorenzoSabatino/WASAText/service/api/reqcontext"
	"github.com/julienschmidt/httprouter"
)

func (rt *_router) postMessage(w http.ResponseWriter, r *http.Request, ps httprouter.Params, ctx reqcontext.RequestContext) {
	// Extract userId and conversationId from URL parameters
	userIdStr := ps.ByName("userId")
	userId, errUsr := strconv.ParseInt(userIdStr, 10, 64)
	conversationIdStr := ps.ByName("conversationId")
	conversationId, errConv := strconv.ParseInt(conversationIdStr, 10, 64)
	if errUsr != nil || errConv != nil || userId <= 0 || conversationId <= 0 {
		ctx.Logger.WithError(errUsr).Error("Invalid userId or conversationId")
		http.Error(w, "Invalid user or conversation id", http.StatusBadRequest)
		return
	}

	// Check if the user exists
	_, err := rt.db.GetUserById(userId)
	if err != nil {
		ctx.Logger.WithError(err).Error("User not found")
		http.Error(w, "User not found", http.StatusNotFound)
		return
	}

	// Check if the user is a member of the conversation
	isMember, err := rt.db.IsUserInConversation(userId, conversationId)
	if err != nil {
		ctx.Logger.WithError(err).Error("Error checking if user is in conversation")
		http.Error(w, "Internal Server Error", http.StatusInternalServerError)
		return
	}
	if !isMember {
		ctx.Logger.Error("User is not a member of the conversation")
		http.Error(w, "Forbidden", http.StatusForbidden)
		return
	}

	// Attempt to retrieve the "content" form value
	content := r.FormValue("content")

	// Check if neither text nor photo was provided
	if content == "" {
		file, _, err := r.FormFile("content")
		if err != nil {
			ctx.Logger.WithError(err).Error("Failed to retrieve photo file")
			http.Error(w, "Failed to process photo", http.StatusBadRequest)
			return
		}
		defer file.Close()

		// Read the photo data into a byte slice
		photoBytes, err := io.ReadAll(file)
		if err != nil {
			ctx.Logger.WithError(err).Error("Failed to read photo file")
			http.Error(w, "Failed to process photo", http.StatusInternalServerError)
			return
		}

		// Handle photo message
		newMessage, err := rt.db.AddMessage(conversationId, userId, "", "received", "photo", photoBytes)
		if err != nil {
			ctx.Logger.WithError(err).Error("Failed to save photo message")
			http.Error(w, "Failed to save message", http.StatusInternalServerError)
			return
		}

		// Respond with the created photo message
		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(http.StatusCreated)
		if err := json.NewEncoder(w).Encode(newMessage); err != nil {
			ctx.Logger.WithError(err).Error("failed to encode response")
			http.Error(w, "Internal Server Error", http.StatusInternalServerError)
			return
		}
		return
	}

	// Handle text message if content is provided
	if content != "" {
		if content == "" {
			ctx.Logger.Error("Text message content is empty")
			http.Error(w, "Text message cannot be empty", http.StatusBadRequest)
			return
		}

		newMessage, err := rt.db.AddMessage(conversationId, userId, content, "received", "text", nil)
		if err != nil {
			ctx.Logger.WithError(err).Error("Failed to save text message")
			http.Error(w, "Failed to save message", http.StatusInternalServerError)
			return
		}

		// Respond with the created message
		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(http.StatusCreated)
		if err := json.NewEncoder(w).Encode(newMessage); err != nil {
			ctx.Logger.WithError(err).Error("failed to encode response")
			http.Error(w, "Internal Server Error", http.StatusInternalServerError)
			return
		}
		return
	}

	// Unsupported content type
	ctx.Logger.Error("Unsupported content type")
	http.Error(w, "Unsupported content type", http.StatusUnsupportedMediaType)
}
