import axios from "axios";

const instance = axios.create({
	baseURL: __API_URL__,
	timeout: 1000 * 5
});

// Helper to set the Authorization header with the user identifier
const setAuthHeader = (userId) => {
	instance.defaults.headers['Authorization'] = `${userId}`;
  };
  
  // Login / Create user method
  export const doLogin = async (username) => {
	try {
	  const response = await instance.post('/session', { name: username });
	  return response.data.id;
	} catch (error) {
	  console.error('Login error:', error);
	  throw error;
	}
  };
  
  // Get all conversations for the logged-in user
  export const getMyConversations = async (userId, sort = "desc") => {
	setAuthHeader(userId);
	try {
	  const response = await instance.get(`/users/${userId}/conversations/`, {
		params: { sort },
	  });
	  return response.data || [];
	} catch (error) {
	  console.error("Error fetching conversations:", error);
	  throw error;
	}
  };
  
  // Get a specific conversation by ID
  export const getConversation = async (userId, conversationId, sortOrder = 'asc') => {
	setAuthHeader(userId);
	try {
	  const response = await instance.get(`users/${userId}/conversations/${conversationId}?sort=${sortOrder}`);
	  return response.data;
	} catch (error) {
	  console.error('Error fetching conversation:', error);
	  throw error;
	}
  };
  
  	// Send a message (text or photo) to a conversation
	export const sendMessage = async (userId, conversationId, messageContent) => {
		setAuthHeader(userId);
		try {
			let response;
			const formData = new FormData();

			// Append the content to the form data as "content"
			formData.append("content", messageContent);

			// Send the form data with the appropriate headers
			response = await instance.post(
				`users/${userId}/conversations/${conversationId}/messages/`, 
				formData, 
				{ headers: { "Content-Type": "multipart/form-data" } }
			);

			return response.data;
		} catch (error) {
			console.error("Error sending message:", error);
			throw error;
		}
	};

  
  // Forward a message to another conversation
  export const forwardMessage = async (userId, conversationId, messageId, targetConversationId) => {
	  setAuthHeader(userId);
	  try {
		const response = await instance.post(`users/${userId}/conversations/${conversationId}/messages/${messageId}/forwardMessage`, {
            conversationId: targetConversationId
		});
		return response.data;
	  } catch (error) {
		console.error('Error forwarding message:', error);
		throw error;
	  }
	};

	// Reply to a specific message in a conversation (text or photo)
	export const replyMessage = async (userId, conversationId, messageId, replyContent) => {
		setAuthHeader(userId);
		try {
			const formData = new FormData();

			// Append the content to the form data as "content"
			formData.append("content", replyContent);

			// Send the form data with the appropriate headers
			const response = await instance.post(
				`users/${userId}/conversations/${conversationId}/messages/${messageId}/replyMessage`,
				formData,
				{ headers: { "Content-Type": "multipart/form-data" } }
			);

			return response.data;
		} catch (error) {
			console.error("Error replying to message:", error);
			throw error;
		}
	};
	
	// Comment on a message (react with an emoticon)
	export const commentMessage = async (userId, conversationId, messageId, comment) => {
	  setAuthHeader(userId);
	  try {
		const response = await instance.put(`users/${userId}/conversations/${conversationId}/messages/${messageId}/comments/`, {
		  content: comment,
		});
		return response.data;
	  } catch (error) {
		console.error('Error commenting on message:', error);
		throw error;
	  }
	};
	
	// Uncomment (remove a comment) from a message
	export const uncommentMessage = async (userId, conversationId, messageId, commentId) => {
	  setAuthHeader(userId);
	  try {
		const response = await instance.delete(`users/${userId}/conversations/${conversationId}/messages/${messageId}/comments/${commentId}`);
		return response.data;
	  } catch (error) {
		console.error('Error removing comment:', error);
		throw error;
	  }
	};
	
	// Delete a message from a conversation
	export const deleteMessage = async (userId, conversationId, messageId) => {
	  setAuthHeader(userId);
	  try {
		const response = await instance.delete(`users/${userId}/conversations/${conversationId}/messages/${messageId}`);
		return response.data;
	  } catch (error) {
		console.error('Error deleting message:', error);
		throw error;
	  }
	};
	
	// Add a user to a group
	export const addToGroup = async (userId, groupId, usernameToAdd) => {
	  setAuthHeader(userId);
	  try {
		const response = await instance.put(`users/${userId}/groups/${groupId}/members/`, {
		  username: usernameToAdd,
		});
		return response.data;
	  } catch (error) {
		console.error('Error adding user to group:', error);
		throw error;
	  }
	};
	
	// Leave a group
	export const leaveGroup = async (userId, groupId) => {
	  setAuthHeader(userId);
	  try {
		const response = await instance.delete(`users/${userId}/groups/${groupId}/members/me`);
		return response.data;
	  } catch (error) {
		console.error('Error leaving group:', error);
		throw error;
	  }
	};
	
	// Set a new group name
	export const setGroupName = async (userId, groupId, newName) => {
	  setAuthHeader(userId);
	  try {
		const response = await instance.put(`users/${userId}/groups/${groupId}/name`, {
		  name: newName,
		});
		return response.data;
	  } catch (error) {
		console.error('Error setting group name:', error);
		throw error;
	  }
	};
	
	// Set a new group photo
	export const setGroupPhoto = async (userId, groupId, photo) => {
		setAuthHeader(userId);
		
		// Create a FormData object
		const formData = new FormData();
		formData.append("photo", photo);
		
		try {
		  const response = await instance.put(`/users/${userId}/groups/${groupId}/photo`, formData, {
			headers: {
			  'Content-Type': 'multipart/form-data',
			  'Authorization': userId,
			},
		  });
		  return response.data;
		} catch (error) {
		  console.error('Error setting group photo:', error);
		  throw error;
		}
	};
	
	// Set a new user photo
	export const setMyPhoto = async (userId, photo) => {
		setAuthHeader(userId);
		
		// Create a FormData object
		const formData = new FormData();
		formData.append("photo", photo);
		
		try {
			const response = await instance.put(`/users/${userId}/photo`, formData, {
				headers: {
					'Content-Type': 'multipart/form-data',
					'Authorization': userId,
				},
			});
			return response.data;
		} catch (error) {
			console.error('Error setting profile photo:', error);
			throw error;
		}
	};
	
	// Set the user's username
	export const setMyUserName = async (userId, newUsername) => {
	  setAuthHeader(userId);
	  try {
		const response = await instance.put(`/users/${userId}/username`, { username: newUsername });
		return response.data;
	  } catch (error) {
		console.error('Error updating username:', error);
		throw error;
	  }
	};

	// Search users by username
	export const searchUsers = async (userId, username) => {
		setAuthHeader(userId);
		try {
		const response = await instance.get(`/users/${userId}/search`, {
			params: { username },
		});
		return response.data || [];
		} catch (error) {
		console.error("Error searching users:", error);
		throw error;
		}
	};
  
	// Add a conversation between two users
	export const addConversation = async (userId, targetUsername, type) => {
		setAuthHeader(userId);
		try {
		const response = await instance.put(`/users/${userId}/conversations/`, {
			targetUsername,
			type,
		});
		return response.data;
		} catch (error) {
		console.error("Error adding conversation:", error);
		throw error;
		}
	};

	// Get all members of a group
	export const getGroupMembers = async (userId, groupId) => {
		setAuthHeader(userId);
		try {
		const response = await instance.get(`/users/${userId}/groups/${groupId}/members/`);
		return response.data || [];
		} catch (error) {
		console.error('Error fetching group members:', error);
		throw error;
		}
};
  
  

export default instance;