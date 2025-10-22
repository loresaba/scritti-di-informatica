<!-- Message.vue -->
<template>
  <div class="mb-3 position-relative d-flex border rounded"
    :class="{ 'justify-content-end': message.sender.userId == userId }"
    @click="handleClick">
    <div class="d-flex flex-column">
      <!-- Forwarded -->
      <div v-if="message.type === 'forward'" class="text-muted fst-italic mb-2">
        <i class="bi bi-arrow-right me-2"></i>
        Forwarded
      </div>

      <!-- Reply to -->
      <div v-if="message.type === 'reply' && replyMessage" class="mb-2">
        <div class="text-muted">
          <i class="bi bi-reply me-2"></i>
          Reply to: <span class="fw-bold">{{ replyMessage.sender.name }}</span>
        </div>
        <div v-if="replyMessage.text">{{ replyMessage.text }}</div>
        <div v-else>Photo <i class="bi bi-image"></i></div>
        <div class="border-top border-muted my-2"></div>
      </div>

      <!-- Sender Information -->
      <div v-if="message.sender.userId != userId"
        class="fw-bold">{{ message.sender.name }}</div>
      
      <!-- Message Content (Text or Photo) -->
      <div>
        <p class="mb-0" v-if="!message.photo">{{ message.text }}</p>
        <img v-if="message.photo" :src="`data:image/png;base64,${message.photo}`" class="img-fluid" alt="Message Photo" />
      </div>

      <!-- Message Status (Checkmarks) -->
      <div class="d-flex align-items-center">
        <small class="text-muted me-2">{{ formatTimestamp(message.timestamp) }}</small>
        <span v-if="message.sender.userId == userId">
          <i v-if="message.status === 'read'" class="bi bi-check-all text-primary"></i>
          <i v-else-if="message.status === 'received'" class="bi bi-check"></i>
          <i v-else class="bi bi-check"></i>
        </span>
      </div>

      <!-- Reactions -->
      <div v-if="reactions.length" class="mt-2">
        <span v-for="reaction in reactions" :key="reaction.sender + reaction.content" class="me-2">
          <strong>{{ reaction.sender }}:</strong> {{ reaction.content }}
        </span>
      </div>
    </div>

    <!-- Context menu -->
    <div
      v-if="showOptions"
      class="position-absolute bg-white border rounded p-2"
      :style="{ top: `${menuPosition.y}px`, left: `${menuPosition.x}px` }"
    >
      <button class="btn btn-light btn-sm d-flex align-items-center mb-1" @click.stop="reply">
        <i class="bi bi-reply me-2"></i> Reply
      </button>
      <button class="btn btn-light btn-sm d-flex align-items-center mb-1" @click.stop="forward">
        <i class="bi bi-arrow-right me-2"></i> Forward
      </button>
      <button v-if="message.sender.userId == userId"
        class="btn btn-light btn-sm d-flex align-items-center" @click.stop="deleteMessage">
        <i class="bi bi-trash me-2"></i> Delete
      </button>
      <button v-if="!hasCommented" class="btn btn-light btn-sm d-flex align-items-center" @click.stop="toggleReactionsMenu">
        <i class="bi bi-emoji-smile me-2"></i> Comment
      </button>
      <button v-if="hasCommented" class="btn btn-light btn-sm d-flex align-items-center" @click.stop="uncomment">
        <i class="bi bi-x me-2"></i> Uncomment
      </button>
    </div>

    <!-- Reactions Menu -->
    <div
      v-if="showReactionsMenu"
      class="position-absolute bg-white border rounded p-2 shadow"
      :style="{ top: `${menuPosition.y}px`, left: `${menuPosition.x}px` }"
    >
      <span
        v-for="emoji in emojis"
        :key="emoji"
        class="d-inline-flex align-items-center justify-content-center me-2 btn btn-outline-secondary p-2"
        style="width: 2.5rem; height: 2.5rem; font-size: 1.5rem; transition: transform 0.2s ease;"
        @click.stop="addReaction(emoji)"
      >
        {{ emoji }}
      </span>
    </div>
  </div>
</template>
  
<script>
  import { deleteMessage, replyMessage, commentMessage, uncommentMessage } from "@/services/axios";
  export default {
    props: {
      message: {
        type: Object,
        required: true,
      },
      conversationId: {
        type: Number,
        required: true,
      },
      messages: {
        type: Array,
        required: true,
      },
    },
    data() {
      return {
        showOptions: false,
        showReactionsMenu: false,
        menuPosition: { x: 0, y: 0 },
        userId: null,
        replyMessage: null,
        emojis: ["😄", "😍", "😢", "👍", "👎"],
        reactions: [],
        hasCommented: false,
      };
    },
    methods: {
      formatTimestamp(timestamp) {
        const date = new Date(timestamp);
        return date.toLocaleString();
      },
      handleClick(event) {
        // Display the context menu at the clicked position
        this.showOptions = true;
        this.showReactionsMenu = false;
        this.menuPosition = {
          x: event.offsetX,
          y: event.offsetY,
        };
      },
      handleOutsideClick(event) {
        // Check if the click occurred outside the component
        if (!this.$el.contains(event.target)) {
          this.showOptions = false;
          this.showReactionsMenu = false;
        }
      },
      toggleReactionsMenu() {
        this.showReactionsMenu = true;
        this.showOptions = false;
      },
      async addReaction(emoji) {
        try {
          console.log("Adding reaction:", emoji);
          const response = await commentMessage(this.userId, this.conversationId, this.message.id, emoji);
          
          this.$emit('add-comment');
          this.gatherReactions();
          this.showReactionsMenu = false;
        } catch (error) {
          console.error("Error adding reaction:", error);
        }
      },
      reply() {
        this.$emit('message-reply', this.message);
      },
      forward() {
        this.$emit('message-forward', this.message);
      },
      async deleteMessage() {
        try {
          console.log("Deleting message:", this.message);
          const response = await deleteMessage(this.userId, this.conversationId, this.message.id);

          this.$emit('message-deleted');
        } catch (error) {
	  			console.error("Error deleating message:", error);
			  }
      },
      findReplyMessage() {
        if (this.message.type === 'reply' && this.message.replyToMessageId) {
          // Search the reply message
          this.replyMessage = this.messages.find(msg => msg.id == this.message.replyToMessageId);
        }
      },
      gatherReactions() {
        if (this.message.comments && Array.isArray(this.message.comments)) {
          this.reactions = this.message.comments.map(comment => ({
            sender: comment.sender.name,
            content: comment.content
          }));
        
          // Check if the user has already commented
          this.hasCommented = this.message.comments.some(
            (comment) => comment.sender.userId == this.userId
          );
        } else {
          this.reactions = [];
          this.hasCommented = false;
        }
      },
      async uncomment() {
        try {
          const comment = this.message.comments.find(
            (comment) => comment.sender.userId == this.userId
          );
          if (comment) {
            console.log("Comment removing", comment.content);
            await uncommentMessage(this.userId, this.conversationId, this.message.id, comment.commentId);
            
            this.$emit('remove-comment');
            this.showOptions = false;
            this.gatherReactions();
          }
        } catch (error) {
          console.error("Error removing comment:", error);
        }
      },
    },
    mounted() {
      const userId = localStorage.getItem("userId");
      this.userId = userId;

      this.findReplyMessage();
      this.gatherReactions();
      // Add a listener to detect clicks outside the component
      document.addEventListener("click", this.handleOutsideClick);
    },
    beforeUnmount() {
      // Remove the listener to avoid memory leaks
      document.removeEventListener("click", this.handleOutsideClick);
    },
    watch: {
      messages: {
        handler(newMessages) {
          this.gatherReactions();
          this.findReplyMessage();
        },
        deep: true,
        immediate: true,
      },
    },
  };
  </script>

<style scoped>

  .mb-3 {
    padding: 10px;
    border-radius: 8px;
    transition: background-color 0.3s ease;
  }

  .mb-3:hover {
    background-color: #c7d9ec;
    cursor: pointer;
  }

  img {
    max-width: 200px;
    max-height: 200px;
    object-fit: contain;
  }

  .position-relative {
    cursor: pointer;
  }

  .position-absolute {
    z-index: 1000;
    min-width: 120px;
    box-shadow: 0px 2px 6px rgba(0, 0, 0, 0.2);
  }

  .btn-outline-secondary:hover {
    background-color: #f0f0f0;
    border-color: #ccc;
    color: #000;
  }
</style>
  