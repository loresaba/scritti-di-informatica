<template>
  <div
    class="modal fade"
    tabindex="-1"
    role="dialog"
    ref="modal"
    aria-labelledby="forwardModalLabel"
    aria-hidden="true"
  >
    <div class="modal-dialog modal-dialog-scrollable" role="document">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title" id="forwardModalLabel">Forward Message</h5>
          <button type="button" class="btn-close" aria-label="Close" @click="close"></button>
        </div>
        <div class="modal-body">
          <div class="text-muted mb-2">
            Your conversations:
          </div>
          <div class="list-group" style="max-height: 270px;">
            <li
              v-for="conversation in conversations"
              :key="conversation.conversationId"
              class="list-group-item mb-2 rounded"
              :class="{'list-group-item-active': selectedConversation && selectedConversation.conversationId === conversation.conversationId}"
              @click="selectConversation(conversation)"
              style="cursor: pointer;"
            >
              <div>{{ conversation.name }}</div>
            </li>
          </div>
          <hr>
          <div class="text-muted mb-2">
            Or select another user to start a new conversation:
          </div>
          <div class="list-group" style="max-height: 270px;">
            <li
              v-for="user in availableUsers"
              :key="user.userId"
              class="list-group-item mb-2 rounded"
              :class="{'list-group-item-active': selectedUser && selectedUser.name === user.name}"
              @click="selectUser(user)"
              style="cursor: pointer;"
            >
              <div>{{ user.name }}</div>
            </li>
          </div>
        </div>
        <div class="modal-footer">
          <button
            type="button"
            class="btn btn-success"
            :disabled="!selectedConversation && !selectedUser"
            @click="forwardMessage"
          >
            Forward
          </button>
          <button type="button" class="btn btn-danger" @click="close">
            Close
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { getMyConversations, forwardMessage, searchUsers, addConversation } from "@/services/axios";

export default {
  props: {
    message: Object,
    conversationId: Number,
  },
  data() {
    return {
      conversations: [],
      availableUsers: [],
      selectedConversation: null,
      selectedUser: null,
    };
  },
  methods: {
    open() {
      const modal = new bootstrap.Modal(this.$refs.modal);
      modal.show();
      this.fetchConversations();
      this.selectedConversation = null;
      this.selectedUser = null;
    },
    close() {
      const modal = bootstrap.Modal.getInstance(this.$refs.modal);
      modal.hide();
      this.selectedConversation = null;
      this.selectedUser = null;
    },
    async fetchConversations() {
      try {
        const userId = localStorage.getItem("userId");
        const response = await getMyConversations(userId);
        this.conversations = response;

        // Recupera gli utenti non ancora nelle conversazioni
        this.fetchAvailableUsers(userId);
      } catch (error) {
        console.error("Errore nel recuperare le conversazioni", error);
      }
    },
    async fetchAvailableUsers(userId) {
      try {
        // Cerca tutti gli utenti, ma filtra quelli già nelle conversazioni
        const allUsers = await searchUsers(userId, '');
        const loggedInUserName = localStorage.getItem("username");
        const userInConversations = this.conversations.map(c => c.name);
        this.availableUsers = allUsers.filter(user => 
          !userInConversations.includes(user.name) && user.name != loggedInUserName
        );
      } catch (error) {
        console.error("Errore nel recuperare gli utenti", error);
      }
    },
    selectConversation(conversation) {
      this.selectedConversation = conversation;
      this.selectedUser = null;
    },
    selectUser(user) {
      this.selectedUser = user;
      this.selectedConversation = null;
    },
    async forwardMessage() {
      try {
        const userId = localStorage.getItem("userId"); // Get the userId
        // API call forward message
        if (this.selectedConversation) {
          await forwardMessage(userId, this.conversationId, this.message.id, this.selectedConversation.conversationId);
        } else if (this.selectedUser) {
          const newConversation = await addConversation(userId, this.selectedUser.name, "direct");
          this.selectedConversation = newConversation;
          await forwardMessage(userId, this.conversationId, this.message.id, this.selectedConversation.conversationId);
        }
        this.$emit("forward-sent");
        this.close();
      } catch (error) {
        console.error("Errore nell'inoltrare il messaggio", error);
      }
    },
  },
};
</script>

<style scoped>
  .modal-content {
    border-radius: 0.5rem;
  }

  .list-group-item-active {
    background-color: #cce5ff;
    color: #004085;
  }

  .list-group-item-active:hover {
    background-color: #b8daff;
  }
</style>
