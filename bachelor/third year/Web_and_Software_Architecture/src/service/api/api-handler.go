package api

import (
	"net/http"
)

// Handler returns an instance of httprouter.Router that handle APIs registered here
func (rt *_router) Handler() http.Handler {
	// Register routes
	rt.router.GET("/", rt.getHelloWorld)
	rt.router.GET("/context", rt.wrap(rt.getContextReply))

	rt.router.POST("/session", rt.wrap(rt.postSession))
	rt.router.PUT("/users/:userId/username", rt.wrap(rt.AuthHandler(rt.putUsername)))
	rt.router.GET("/users/:userId/conversations/", rt.wrap(rt.AuthHandler(rt.getConversations)))
	rt.router.PUT("/users/:userId/conversations/", rt.wrap(rt.AuthHandler(rt.addConversation)))
	rt.router.GET("/users/:userId/conversations/:conversationId", rt.wrap(rt.AuthHandler(rt.getConversation)))
	rt.router.POST("/users/:userId/conversations/:conversationId/messages/", rt.wrap(rt.AuthHandler(rt.postMessage)))
	rt.router.POST("/users/:userId/conversations/:conversationId/messages/:messageId/forwardMessage", rt.wrap(rt.AuthHandler(rt.forwardMessage)))
	rt.router.POST("/users/:userId/conversations/:conversationId/messages/:messageId/replyMessage", rt.wrap(rt.AuthHandler(rt.replyMessage)))
	rt.router.PUT("/users/:userId/conversations/:conversationId/messages/:messageId/comments/", rt.wrap(rt.AuthHandler(rt.commentMessage)))
	rt.router.DELETE("/users/:userId/conversations/:conversationId/messages/:messageId/comments/:commentId", rt.wrap(rt.AuthHandler(rt.uncommentMessage)))
	rt.router.DELETE("/users/:userId/conversations/:conversationId/messages/:messageId", rt.wrap(rt.AuthHandler(rt.deleteMessage)))
	rt.router.PUT("/users/:userId/groups/:groupId/members/", rt.wrap(rt.AuthHandler(rt.addToGroup)))
	rt.router.DELETE("/users/:userId/groups/:groupId/members/me", rt.wrap(rt.AuthHandler(rt.leaveGroup)))
	rt.router.PUT("/users/:userId/groups/:groupId/name", rt.wrap(rt.AuthHandler(rt.setGroupName)))
	rt.router.PUT("/users/:userId/photo", rt.wrap(rt.AuthHandler(rt.setMyPhoto)))
	rt.router.PUT("/users/:userId/groups/:groupId/photo", rt.wrap(rt.AuthHandler(rt.setGroupPhoto)))
	rt.router.GET("/users/:userId/search", rt.wrap(rt.AuthHandler(rt.searchUsers)))
	rt.router.GET("/users/:userId/groups/:groupId/members/", rt.wrap(rt.AuthHandler(rt.getGroupMembers)))

	// Special routes
	rt.router.GET("/liveness", rt.liveness)

	return rt.router
}
