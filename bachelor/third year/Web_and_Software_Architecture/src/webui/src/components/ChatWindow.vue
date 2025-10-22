<script>
import { getConversation, sendMessage, leaveGroup, replyMessage, setGroupPhoto } from "@/services/axios";
import Message from './Message.vue';

export default {
	components: {
    	Message,
  	},
	props: {
		conversation: {
			type: Object,
			required: true,
		},
	},
	data() {
		return {
			newMessage: "",
			messages: [],
			replyTo: null,
			localConversation: { ...this.conversation },
		};
	},
	methods: {
	  async sendNewMessage() {
	  if (this.newMessage.trim()) {
		try {
		  // If replying to a message, use the reply-specific method
		  if (this.replyTo) {
          	await this.sendReplyMessage();
            return;
          }

		  // Send a standard new message
		  const userId = localStorage.getItem("userId");
		  const conversationId = this.conversation.conversationId;
					
		  // Call the sendMessage API function to send the new message
		  console.log("Sending messages: ", this.newMessage);
		  await sendMessage(userId, conversationId, this.newMessage);
				
		  // Update the messages list with the newly sent message
		  this.getMessages();
					
		  // Reset the input field
		  this.newMessage = "";

		  // Emit event to parent to refresh the conversations
		  this.$emit('message-sent');
		  } catch (error) {
		  	console.error("Error sending message:", error);
		  }
	  	}
	  },
	  // Handles sending a reply message
	  async sendReplyMessage() {
      try {
        const userId = localStorage.getItem("userId");
        const conversationId = this.conversation.conversationId;

        console.log("Sending reply message:", this.newMessage);
        await replyMessage(userId, conversationId, this.replyTo.id, this.newMessage);
        

        // Refresh the messages list and reset fields
        this.getMessages();
        this.newMessage = "";
        this.replyTo = null;

        // Emit an event to notify the parent component
        this.$emit('message-sent');
      } catch (error) {
        console.error("Error sending reply message:", error);
      }
		},
		async getMessages() {
			try {
				const conversationId = this.conversation.conversationId;
				const userId = localStorage.getItem("userId");

				console.log("Getting messages");
				const conversation = await getConversation(userId, conversationId);
				this.messages = conversation.messages;

				// Scroll to the bottom after fetching the messages
				this.scrollToBottom();
				this.$emit('message-sent');
			} catch (error) {
				console.error("Error fetching messages:", error);
			}
		},
		scrollToBottom() {
      		const messagesContainer = this.$refs.messagesContainer;
      		messagesContainer.scrollTop = messagesContainer.scrollHeight; // Scroll to the bottom
    	},
		addMember() {
			this.$emit('add-member', this.conversation);
		},
		async leaveGroup() {
		  try {
			// Get the user ID from local storage
			const userId = localStorage.getItem("userId");
			const groupId = this.conversation.conversationId;

			// Call the API to leave the group
			console.log(`User ${userId} is leaving group ${groupId}`);
			await leaveGroup(userId, groupId);

			// Emit an event to notify the parent component about the action
			this.$emit('leave-group');
		  } catch (error) {
			  // Log the error to the console for debugging
			  console.error("Error leaving the group:", error);
			}
		},
		handleReply(message) {
		  // Set the reply information
		  this.replyTo = message;
		},
		handleForward(message) {
		  this.$emit('message-forward', { message, conversationId: this.conversation.conversationId });
		},
		cancelReply() {
      	  // Reset the reply state
      	  this.replyTo = null;
    	},
		changeGroupName() {
		  this.$emit('change-group-name', this.conversation);
		},
		changeGroupPhoto() {
      		this.$refs.groupPhotoUploadInput.click();
    	},
		async handleGroupPhotoUpload(event) {
			try {
				const file = event.target.files[0];
				if (file) {
					const groupId = this.conversation.conversationId;
					const userId = localStorage.getItem("userId");
					if (!userId || !groupId) {
						console.error("User ID or Group ID not found. Please log in again.");
						return;
					}

					// Send the photo file to the server
					const response = await setGroupPhoto(userId, groupId, file)
					// Update the group photo
					this.localConversation.photo = response.photo;

					this.$emit('change-group-photo');
					console.log("Group photo updated successfully!");
				}
			} catch (error) {
				console.error("Error uploading group photo:", error);
			}
		},
		openPhotoUpload() {
			this.$refs.photoUploadInput.click();
		},
		async sendPhoto(event) {
			try {
				const file = event.target.files[0];
				if (file) {
					const userId = localStorage.getItem("userId");
					const conversationId = this.conversation.conversationId;

					console.log("Sending photo message...");
					
					if (this.replyTo) {      
      					await replyMessage(userId, conversationId, this.replyTo.id, file);
      					this.replyTo = null; // Resetta lo stato di risposta dopo l'invio
    				} else {
      					await sendMessage(userId, conversationId, file);
    				}
					this.getMessages();
				}
			} catch (error) {
				console.error("Error sending photo message:", error);
			}
		},
	},
	watch: {
		conversation: {
		handler(newConversation) {
			this.localConversation = { ...newConversation };
			this.getMessages(); // Fetch messages for the new conversation
		},
		immediate: true, // Run the handler immediately after mounting
		},
	},
	mounted() {
		this.getMessages();
	},
	updated() {
    	this.scrollToBottom();
  	},
};
</script>

<template>
	<div class="d-flex flex-column h-100">
		<!-- Header -->
		<div class="d-flex justify-content-between align-items-center p-3 border-bottom">
			<h5 class="mb-0">{{ conversation.name }}</h5>

			<!-- Group buttons -->
			<div v-if="conversation.type === 'group'">
				<button class="btn btn-outline-primary btn-sm me-2" @click="changeGroupName">Change Group Name</button>
				<button class="btn btn-outline-primary btn-sm me-2" @click="changeGroupPhoto">Change Group Photo</button>
				<button class="btn btn-outline-primary btn-sm me-2" @click="addMember">Add Member</button>
				<button class="btn btn-outline-danger btn-sm" @click="leaveGroup">Leave Group</button>
			</div>
		</div>

		<!-- Messages -->
		<div
      		ref="messagesContainer"
      		class="flex-grow-1 overflow-auto p-3"
      		style="background-color: #f0f0f0; scroll-behavior: smooth;">
		<message
			v-for="message in messages"
			:key="message.id"
			:message="message"
			:conversation-id="conversation.conversationId"
			@message-deleted="getMessages"
			@message-reply="handleReply"
  			@message-forward="handleForward"
			@add-comment="getMessages"
			@remove-comment="getMessages"
      		:messages="messages"
		/>
		</div>

		 <!-- Reply Information -->
		<div v-if="replyTo" class="border-top p-2 bg-light d-flex align-items-center">
		  <span class="text-muted me-auto">
			Replying to <strong>{{ replyTo.sender.name }}</strong>: {{ replyTo.content }}
		  </span>
		  <button class="btn btn-sm btn-outline-secondary" @click="cancelReply">x</button>
		</div>

		<!-- Message Input -->
		<div class="border-top p-3">
			<div class="input-group">
				<input
					type="text"
					class="form-control"
					placeholder="Type a message"
					v-model="newMessage"
					@keyup.enter="sendNewMessage"
				/>

				<button class="btn btn-outline-secondary" @click="openPhotoUpload">
					<i class="bi bi-image"></i>
				</button>

				<!-- Hidden file input for photo upload -->
				<input
					ref="photoUploadInput"
					type="file"
					accept="image/*"
					style="display: none;"
					@change="sendPhoto"
				/>

				<button class="btn btn-primary" @click="sendNewMessage">Send</button>
			</div>
		</div>
	
	<!-- Upload the group photo -->
	<input
		ref="groupPhotoUploadInput"
		type="file"
		accept="image/*"
		style="display: none;"
		@change="handleGroupPhotoUpload"
  	/>
	</div>
</template>

<style scoped>
.flex-grow-1 {
	overflow-y: auto;
	max-height: calc(100vh - 150px);
}
</style>
