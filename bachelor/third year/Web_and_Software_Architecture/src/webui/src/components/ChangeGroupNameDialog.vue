<template>
  <div v-if="isOpen" class="modal fade show d-block" tabindex="-1" role="dialog" aria-labelledby="changeGroupNameLabel" aria-hidden="true">
    <div class="modal-dialog" role="document">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title" id="changeGroupNameLabel">Change Group Name</h5>
          <button type="button" class="btn-close" aria-label="Close" @click="closeDialog"></button>
        </div>
        <div class="modal-body">
          <form @submit.prevent="submitGroupNameChange">
            <div class="mb-3">
              <label for="groupNameInput" class="form-label">New Group Name</label>
              <input
                type="text"
                id="groupNameInput"
                class="form-control"
                v-model="newGroupName"
                placeholder="Enter new group name"
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
  props: {
    conversation: Object,
  },
  emits: ['group-name-changed'],
  data() {
    return {
      isOpen: false,
      newGroupName: '',
    };
  },
  methods: {
    open() {
      this.newGroupName = this.conversation?.name || '';
      this.isOpen = true;
    },
    closeDialog() {
      this.isOpen = false;
      this.newGroupName = '';
    },
    submitGroupNameChange() {
      if (this.newGroupName.trim() === "") {
        alert("Group name cannot be empty.");
        return;
      }
      this.$emit('group-name-changed', this.newGroupName.trim());
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
