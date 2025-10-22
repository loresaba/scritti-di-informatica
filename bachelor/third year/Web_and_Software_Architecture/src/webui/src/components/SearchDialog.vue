<template>
  <div
    class="modal fade"
    tabindex="-1"
    role="dialog"
    ref="modal"
    aria-labelledby="searchUsersModalLabel"
    aria-hidden="true"
  >
    <div class="modal-dialog" role="document">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title" id="searchUsersModalLabel">Search Users</h5>
          <button type="button" class="btn-close" aria-label="Close" @click="close"></button>
        </div>
        <div class="modal-body">
          <input
            type="text"
            v-model="searchQuery"
            class="form-control mb-3"
            placeholder="Search by username"
            @input="onSearchInput"
            @select.stop=""
          />
          <div class="list-group overflow-auto" style="max-height: 270px;">
            <li
              v-for="user in searchResults"
              :key="user.userId"
              class="list-group-item d-flex align-items-center"
              :class="{'list-group-item-primary': selectedUser && selectedUser.userId == user.userId}"
              @click="selectUser(user)"
              style="cursor: pointer;"
            >
              <div class="d-flex align-items-center">
                <div class="user-photo me-3">
                  <img
                    v-if="user.photo"
                    :src="`data:image/png;base64,${user.photo}`"
                    alt="User Photo"
                    class="img-fluid"
                  />
                  <i
                    v-else
                    class="bi bi-person-circle fs-4 text-secondary"
                  ></i>
                </div>
                <div>
                  <span>{{ user.name }}</span>
                  <span
                    v-if="user.userId == userId"
                    class="badge bg-info text-dark ms-2"
                  >
                    YOU
                  </span>
                </div>
              </div>
            </li>
          </div>
        </div>
        <div class="modal-footer">
          <button
            type="button"
            class="btn btn-success me-2"
            :disabled="!selectedUser"
            @click="startConversation"
          >
            Start conversation
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
  import { searchUsers } from "@/services/axios";
  
  export default {
    data() {
      return {
        searchQuery: "",
        searchResults: [],
        debounceTimeout: null,
        userId: null,
        selectedUser: null,
      };
    },
    methods: {
      open() {
        const modal = new bootstrap.Modal(this.$refs.modal);
        modal.show();
        this.reset();

        // Get userId from localStorage
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
        this.searchQuery = "";
        this.searchResults = [];
        this.selectedUser = null;
      },
      onSearchInput() {
        if (this.debounceTimeout) {
          clearTimeout(this.debounceTimeout);
        }
  
        // Start a new debounce timer
        this.debounceTimeout = setTimeout(() => {
          this.search();
        }, 500); // 500ms delay after the user stops typing
      },
      async search() {
        try {
          console.log("Searching users");
          const results = await searchUsers(this.userId, this.searchQuery);
          this.searchResults = results;
        } catch (error) {
          console.error("Error searching users:", error);
        }
      },
      selectUser(user) {
        this.selectedUser = user;
      },
      startConversation() {
        if (this.selectedUser) {
          if (this.selectedUser.userId == this.userId) {
            alert("You cannot start a conversation with yourself. Please select another user.");
            return;
          }
          this.$emit("select", this.selectedUser);
          this.close();
        }
      },
    },
  };
  </script>
  
<style scoped>
  .list-group-item-primary {
    background-color: #cce5ff;
  }
  
  .list-group-item-primary:hover {
    background-color: #b8daff;
  }

  .modal-content {
   border-radius: 0.5rem;
  }

  .user-photo img {
    width: 40px;
    height: 40px;
    object-fit: cover;
    border-radius: 50%;
  }
 </style>