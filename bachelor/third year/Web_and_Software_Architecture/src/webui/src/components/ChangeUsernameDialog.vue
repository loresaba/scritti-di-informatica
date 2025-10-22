<template>
    <div v-if="isOpen" class="modal fade show d-block" tabindex="-1" role="dialog" aria-labelledby="changeUsernameLabel" aria-hidden="true">
      <div class="modal-dialog" role="document">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title" id="changeUsernameLabel">Change Username</h5>
            <button type="button" class="btn-close" aria-label="Close" @click="closeDialog"></button>
          </div>
          <div class="modal-body">
            <form @submit.prevent="submitUsernameChange">
              <div class="mb-3">
                <label for="usernameInput" class="form-label">New Username</label>
                <input
                  type="text"
                  id="usernameInput"
                  class="form-control"
                  v-model="newUsername"
                  placeholder="Enter new username"
                  required
                />
              </div>
              <div class="d-flex justify-content-end">
                <button type="button" class="btn btn-secondary me-2" @click="closeDialog">Cancel</button>
                <button type="submit" class="btn btn-primary">Change</button>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>
    <div v-if="isOpen" class="modal-backdrop fade show"></div>
  </template>
  
  <script>
  export default {
    emits: ["username-changed"],
    data() {
      return {
        isOpen: false,
        newUsername: "",
      };
    },
    methods: {
      open() {
        this.newUsername = localStorage.getItem("username") || "";
        this.isOpen = true;
      },
      closeDialog() {
        this.isOpen = false;
        this.newUsername = "";
      },
      submitUsernameChange() {
        if (this.newUsername.trim() === "") {
          alert("Username cannot be empty.");
          return;
        }
        this.$emit("username-changed", this.newUsername.trim());
        this.closeDialog();
      },
    },
  };
  </script>
  
  <style scoped>
  .modal-backdrop {
    z-index: 1040;
  }
  
  .modal {
    z-index: 1050;
  }
  </style>
  