<template>
	<li
	  class="list-group-item list-group-item-action"
	  :class="{ active: isActive }"
	  @click="selectConversation"
	>
	  <div class="d-flex align-items-center">
		<!-- Profile Photo or Group Icon -->
		<img
		  v-if="conversation.photo"
		  :src="`data:image/png;base64,${conversation.photo}`"
		  alt="Profile Photo"
		  class="rounded-circle me-3"
		  style="width: 40px; height: 40px; object-fit: cover;"
		/>
		<i v-else class="bi bi-person-circle me-3" style="font-size: 30px;"></i>
		
		<div>
		  <!-- Conversation Name -->
		  <h5 class="mb-1">{{ conversation.name }}</h5>
		  <!-- Message Preview -->
		  <p class="mb-0 text-truncate">
			{{ getLastMessagePreview(conversation) }}
		  </p>
		  <!-- Timestamp of Latest Message -->
		  <small 
		 	:class="{ 'text-muted': !isActive, 'text-white': isActive }" 
		  >{{ formatTimestamp(conversation.lastMessage?.timestamp) }}</small>
		</div>
	  </div>
	</li>
  </template>
  
  <script>
  export default {
	props: {
	  conversation: {
		type: Object,
		required: true,
	  },
	  isActive: {
		type: Boolean,
		default: false,
	  },
	},
	methods: {
	  selectConversation() {
		this.$emit("select", this.conversation);
	  },
	  getLastMessagePreview(conversation) {
		if (conversation.lastMessage) {
			const lastMessage = conversation.lastMessage;
			// If the last message is a photo, return a specific icon or message
			if (!lastMessage.preview) {
			return "📸 Photo message";
			}
			// Truncate the preview if it's longer than 28 characters
			if (lastMessage.preview.length > 28) {
				return lastMessage.preview.substring(0, 28) + '...';
			}
			return lastMessage.preview;
		}
		return 'No message yet';
	  },
	  formatTimestamp(timestamp) {
		if (timestamp) {
			const date = new Date(timestamp);
			return date.toLocaleString(); // Format timestamp into a readable date
		}
		return 'No timestamp';
	  },
	},
  };
  </script>
  
  <style scoped>
  .list-group-item {
	cursor: pointer;
  }
  .list-group-item.active {
	background-color: #007bff;
	color: #fff;
  }
  .text-truncate {
	overflow: hidden;
	white-space: nowrap;
	text-overflow: ellipsis;
  }
  </style>
  