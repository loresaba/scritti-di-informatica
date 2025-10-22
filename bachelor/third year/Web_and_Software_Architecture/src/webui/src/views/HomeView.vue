// ChatView.vue
<script>
import { getMyConversations, addConversation, addToGroup, setMyUserName, setGroupName, setMyPhoto, searchUsers  } from "@/services/axios";
import ConversationList from "@/components/ConversationList.vue";
import ChatWindow from "@/components/ChatWindow.vue";
import SearchDialog from "@/components/SearchDialog.vue";
import NewGroupDialog from "@/components/NewGroupDialog.vue";
import AddMemberDialog from "@/components/AddMemberDialog.vue";
import ForwardModal from "../components/ForwardModal.vue";
import ChangeUsernameDialog from "../components/ChangeUsernameDialog.vue";
import ChangeGroupNameDialog from "../components/ChangeGroupNameDialog.vue";

export default {
  components: { ConversationList, ChatWindow, SearchDialog, NewGroupDialog, AddMemberDialog, ForwardModal, ChangeUsernameDialog, ChangeGroupNameDialog },
  data() {
    return {
      conversations: [],
      selectedConversation: null,
      errormsg: null,
      loading: false,
      messageToForward: null,
      conversationIdToForward: null,
      username: localStorage.getItem("username"),
      userPhoto: localStorage.getItem("userPhoto"),
      groupChangeName: null,
    };
  },
  methods: {
    async refresh() {
		  this.loading = true;
      this.errormsg = null;

      try {
        const userId = localStorage.getItem("userId"); // Get the userId
        if (!userId) {
          throw new Error("User ID not found. Please log in again.");
        }

        // Make the API call
        console.log("Getting conversations");
        const response = await getMyConversations(userId);
        this.conversations = response;
      } catch (error) {
        console.error("Error refreshing conversations:", error);
        this.errormsg = "Failed to load conversations.";
      } finally {
        this.loading = false;
      }
    },
    selectConversation(conversation) {
      this.selectedConversation = conversation;
    },
    searchUser() {
      // Open the search dialog
      this.$refs.searchDialog.open();
    },
    async selectUserFromSearch(user) {
      console.log("User selected from search:", user);
      try {
        // Call the API to create the conversation
        const userId = localStorage.getItem("userId");
        if (!userId) {
          throw new Error("User ID not found. Please log in again.");
        }
        
        // Create the new conversation
        await addConversation(userId, user.name, "direct");
        
        // Refresh the conversation list after creating the conversation
        await this.refresh();
      } catch (error) {
        console.error("Error starting conversation:", error);
        this.errormsg = "Failed to create conversation.";
      }
    },
    newGroup() {
      this.$refs.newGroupDialog.open();
    },
    async handleCreateGroup(groupData) {
      console.log("Creating group:", groupData);
      try {
        const userId = localStorage.getItem("userId");
        if (!userId) {
          throw new Error("User ID not found. Please log in again.");
        }

        // Add the first user to the group
        const groupMember = groupData.users[0];
        const groupResponse = await addConversation(userId, groupMember.name, "group");

        // Set the group name
        const groupId = groupResponse.conversationId;
        await setGroupName(userId, groupId, groupData.name);

        // Add other users to the group
        const otherUsers = groupData.users.slice(1);
        for (const user of otherUsers) {
          await addToGroup(userId, groupId, user.name);
        }

        // Refresh after creating the group
        await this.refresh();
      } catch (error) {
        console.error("Error creating group:", error);
        this.errormsg = "Failed to create group.";
      }
    },
    refreshAndDeselect() {
      this.refresh();
      this.selectedConversation = null;
    },
    handleAddMember(conversation) {
      this.$refs.addMemberDialog.open(conversation.conversationId);
    },
    handleForwardMessage({ message, conversationId }) {
      this.messageToForward = message;
      this.conversationIdToForward = conversationId;
      this.$refs.forwardModal.open();
    },
    changeUsername() {
      this.$refs.changeUsernameDialog.open();
    },
    async handleUsernameChange(newUsername) {
      try {
        // Retrieve the user ID from localStorage
        const userId = localStorage.getItem("userId");
        if (!userId) {
          throw new Error("User ID not found. Please log in again.");
        }
        // Call the API to update the username
        await setMyUserName(userId, newUsername);

        // Update the username
        this.username = newUsername;
        localStorage.setItem("username", newUsername);
        console.log(`Username updated successfully ${newUsername}`);
      } catch (error) {
        console.error("Error changing username:", error);
        alert(error.response.data);
      }
    },
    changeGroupName(conversation) {
      this.groupChangeName = conversation;
      this.$refs.changeGroupNameDialog.open();
    },
    async handleGroupNameChanged(newGroupName) {
      try {
        const userId = localStorage.getItem("userId");
        const groupId = this.groupChangeName.conversationId;
        if (!userId || !groupId) {
          throw new Error("User ID or Group ID not found.");
        }

        // Call the API to set the new group name
        await setGroupName(userId, groupId, newGroupName);

        // Update the group's name in the local state or refetch the conversation
        console.log(`Group name updated to: ${newGroupName}`);
        this.selectedConversation.name = newGroupName;
        await this.refresh();
      } catch (error) {
        console.error("Error updating group name:", error);
        this.errormsg = "Failed to update group name.";
      }
    },
    openPhotoDialog() {  
      this.$refs.photoUploadInput.click();
    },
    async handlePhotoUpload(event) {
      try {
        const file = event.target.files[0];
        if (file) {
          const userId = localStorage.getItem("userId");
          if (!userId) {
            console.error("User ID not found. Please log in again.");
            return;
          }

          // Send the photo file to the server
          const response = await setMyPhoto(userId, file);
          localStorage.setItem("userPhoto", response.photo);
          this.userPhoto = response.photo;

          console.log("Photo updated successfully!");
        }
      } catch (error) {
        console.error("Error uploading photo:", error);
      }
    },
    async setUserPhoto(username) {
      try {
        const userId = localStorage.getItem("userId");
        if (!userId) {
          throw new Error("User ID not found. Please log in again.");
        }

        const userDetails = await searchUsers(userId, username);

        if (userDetails[0].photo) {
          const photo = userDetails[0].photo;
          
          localStorage.setItem("userPhoto", photo);
          this.userPhoto = photo;
        }
      } catch (error) {
        console.error("Error setting user photo from search:", error);
        this.errormsg = "Failed to retrieve user photo.";
      }
    },
    logout() {
      const username = localStorage.getItem("username");

      localStorage.removeItem("userId");
      localStorage.removeItem("username");
      localStorage.removeItem("userPhoto");

      console.log(`${username} logged out successfully.`);

      this.$router.push("/login");
    },
  },
  mounted() {
    this.setUserPhoto(this.username);
    this.refresh();
  },
};
</script>


<template>
	<div class="container-fluid vh-100">
    <!-- User Bar -->
    <div class="row bg-custom-gray text-dark py-2 px-4 align-items-center" style="height: 10%;">
      <div class="col d-flex align-items-center">
        <img v-if="userPhoto" :src="`data:image/png;base64,${userPhoto}`" alt="User Photo" class="user-photo me-2" style="width: 40px; height: 40px; border-radius: 50%; object-fit: cover;" />
        <i v-else class="bi bi-person-circle me-2" style="font-size: 30px;"></i>
        <h5 class="mb-0">{{ username }}</h5>
      </div>
      <div class="col text-end">
        <button class="btn btn-light btn-sm" @click="changeUsername">Change Username</button>
        <button v-if="!userPhoto" class="btn btn-light btn-sm ms-2" @click="openPhotoDialog">Add Photo</button>
        <button v-if="userPhoto" class="btn btn-light btn-sm ms-2" @click="openPhotoDialog">Change Photo</button>
        <button class="btn btn-danger btn-sm ms-2" @click="logout">Logout</button>
      </div>
    </div>
    
	  <div class="row" style="height: 90%;">
      <!-- Conversation List -->
      <div class="col-md-4 col-lg-3 border-end px-0 bg-light" style="height: 100%;">
        <ConversationList
        :conversations="conversations"
        :selectedConversation="selectedConversation"
        @select="selectConversation"
        @refresh="refresh"
        @search="searchUser"
        @new="newGroup"
        />
      </div>
    
      <!-- Chat Window -->
      <div class="col-md-8 col-lg-9 px-0" style="height: 100%;">
        <ChatWindow v-if="selectedConversation" :conversation="selectedConversation" @message-sent="refresh"
          @leave-group="refreshAndDeselect" @add-member="handleAddMember"
          @message-forward="handleForwardMessage" @change-group-name="changeGroupName"
          @change-group-photo="refresh"
        />
        <div v-else class="h-100 d-flex align-items-center justify-content-center">
          <p class="text-muted">Select a conversation to view messages</p>
        </div>
      </div>
	  </div>
  
	  <ErrorMsg v-if="errormsg" :msg="errormsg" />

    <!-- Search Dialog -->
    <SearchDialog
      ref="searchDialog"
      @select="selectUserFromSearch"
    />
    <!-- New Group Dialog -->
    <NewGroupDialog
      ref="newGroupDialog"
      @create-group="handleCreateGroup"
    />
    <!-- Add Member Dialog -->
    <AddMemberDialog
      ref="addMemberDialog"
    />

    <!-- Froward Modal -->
    <ForwardModal
      ref="forwardModal"
      :conversationId="conversationIdToForward"
      :message="messageToForward"
      @forward-sent="refreshAndDeselect"
    />

    <!-- Change Username Dialog -->
    <ChangeUsernameDialog
      ref="changeUsernameDialog"
      @username-changed="handleUsernameChange"
    />

    <!-- Change GroupName Dialog -->
    <ChangeGroupNameDialog
		  :conversation="groupChangeName"
		  @group-name-changed="handleGroupNameChanged"
		  ref="changeGroupNameDialog"
	  />

    <!-- Upload the photo -->
    <input
      ref="photoUploadInput"
      type="file"
      accept="image/*"
      style="display: none;"
      @change="handlePhotoUpload"
    />
	</div>
</template>

<style scoped>
.border-end {
  border-right: 1px solid #ddd;
}

.bg-custom-gray {
  background-color: #ddd;
}
</style>
