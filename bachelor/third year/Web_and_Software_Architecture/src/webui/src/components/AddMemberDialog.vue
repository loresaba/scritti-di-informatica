<template>
    <div
      class="modal fade"
      tabindex="-1"
      role="dialog"
      ref="modal"
      aria-labelledby="addMemberModalLabel"
      aria-hidden="true"
    >
      <div class="modal-dialog" role="document">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title" id="addMemberModalLabel">Add Members</h5>
            <button type="button" class="btn-close" @click="close"></button>
          </div>
          <div class="modal-body">
            <div class="mb-3">
              <label class="form-label">Select Users</label>
              <div class="user-list">
                <div
                  v-for="user in searchResults"
                  :key="user.userId"
                  class="user-item d-flex justify-content-between align-items-center"
                  @click="toggleUserSelection(user)"
                >
                  <div class="d-flex align-items-center">
                    <div class="user-photo me-3">
                      <img v-if="user.photo" :src="`data:image/png;base64,${user.photo}`" alt="User Photo" />
                      <i v-else class="bi bi-person-circle" style="font-size: 30px;"></i>
                    </div>
                    <span>{{ user.name }}</span>
                    <span v-if="user.userId === userId" class="badge">YOU</span>
                  </div>
                  <input
                    type="checkbox"
                    :value="user.userId"
                    :checked="isSelected(user)"
                    @click.stop="toggleUserSelection(user)"
                  />
                </div>
              </div>
            </div>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" @click="close">Close</button>
            <button
              type="button"
              class="btn btn-primary"
              @click="addMembers"
              :disabled="selectedUsers.length === 0"
            >
              Add Members
            </button>
          </div>
        </div>
      </div>
    </div>
  </template>
  
  <script>
  import { searchUsers, getGroupMembers, addToGroup } from "@/services/axios";
  
  export default {
    data() {
      return {
        conversationId: null,
        selectedUsers: [],
        searchResults: [],
        userId: null,
      };
    },
    methods: {
      open(conversationId) {
        this.conversationId = conversationId;
        const modal = new bootstrap.Modal(this.$refs.modal);
        modal.show();
        this.reset();
  
        const userId = localStorage.getItem("userId");
        this.userId = userId;
        this.search();
      },
      close() {
        const modal = bootstrap.Modal.getInstance(this.$refs.modal);
        modal.hide();
        this.reset();
      },
      reset() {
        this.selectedUsers = [];
        this.searchResults = [];
      },
      async search() {
        try {
          const allUsers = await searchUsers(this.userId, "");
          const groupMembers = await getGroupMembers(this.userId, this.conversationId);

          // Filter users to exclude those already in the group
          this.searchResults = allUsers.filter(user => !groupMembers.includes(user.name));
        } catch (error) {
          console.error("Error searching users:", error);
        }
      },
      isSelected(user) {
        return this.selectedUsers.some(
          (selectedUser) => selectedUser.userId === user.userId
        );
      },
      toggleUserSelection(user) {
        if (user.userId === this.userId) {
          alert("You cannot add yourself. You are already in the group.");
          return;
        }
        const index = this.selectedUsers.findIndex(
          (selectedUser) => selectedUser.userId === user.userId
        );
        if (index === -1) {
          this.selectedUsers.push(user);
        } else {
          this.selectedUsers.splice(index, 1);
        }
      },
      async addMembers() {
        try {
          const userId = localStorage.getItem("userId");
          for (const user of this.selectedUsers) {
            await addToGroup(userId, this.conversationId, user.name);
          }
          this.close();
        } catch (error) {
          console.error("Error adding members:", error);
        }
      },
    },
  };
  </script>
  
<style scoped>
  .user-photo img {
    width: 40px;
    height: 40px;
    object-fit: cover;
    border-radius: 50%;
  }
</style>
  