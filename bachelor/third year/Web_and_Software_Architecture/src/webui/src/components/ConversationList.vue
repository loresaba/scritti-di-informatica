<script>
  import Conversation from "@/components/Conversation.vue";
  
  export default {
    components: {
      Conversation,
    },
    props: {
      conversations: {
        type: Array,
        required: true,
      },
      selectedConversation: {
        type: Object,
        default: null,
      },
    },
    methods: {
      selectConversation(conversation) {
        this.$emit("select", conversation);
      },
    },
  };
  </script>

<template>
  <div>
    <div class="d-flex justify-content-between align-items-center p-3 border-bottom">
      <h2 class="h5">Conversations</h2>
      <div class="d-flex">
        <button class="btn btn-sm btn-outline-secondary me-2" @click="$emit('refresh')">
          <i class="bi bi-arrow-clockwise"></i>
        </button>
        <button class="btn btn-sm btn-outline-primary me-2" @click="$emit('search')">
          <i class="bi bi-search"></i>
        </button>
        <button class="btn btn-sm btn-outline-success" @click="$emit('new')">
          <i class="bi bi-plus-circle"></i>
        </button>
      </div>
    </div>
    <ul class="list-group list-group-flush">
      <Conversation
        v-for="conversation in conversations"
        :key="conversation.conversationId"
        :conversation="conversation"
        :isActive="selectedConversation && selectedConversation.conversationId === conversation.conversationId"
        @select="selectConversation(conversation)"
      />
    </ul>
  </div>
</template>
  
<style scoped>
  .border-bottom {
    border-bottom: 1px solid #ddd;
  }
</style>
  