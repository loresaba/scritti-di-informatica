<script>
import { doLogin } from "@/services/axios";

export default {
  data() {
    return {
      form: {
        username: "",
      },
    };
  },
  methods: {
    async handleSubmit() {
      try {
        console.log("Login submitted:", this.form);

        // Make the API call
        const response = await doLogin(this.form.username);
        const userId = response;

        // Save the userId, username e userPhoto
        localStorage.setItem("userId", userId);
        console.log("Login successful, User ID:", userId);
        localStorage.setItem("username", this.form.username);
        localStorage.setItem("userPhoto", "");


        // Redirect to the Home page
        this.$router.push("/home");
      } catch (error) {
        console.error("Login failed:", error);
        alert("Login failed. Please try again.");
      }
    },
  },
};
</script>

<template>
  <div class="container d-flex align-items-center justify-content-center vh-100">
    <div class="card p-4 shadow-sm" style="width: 100%; max-width: 400px;">
      
      <!-- Logo Section -->
      <div class="d-flex justify-content-center mb-3">
        <img src="@/assets/logo.png" alt="WasaText Logo" class="logo-img" />
      </div>

      <!-- App Title -->
      <h2 class="text-center mb-2">WasaText</h2>

      <!-- App Description -->
      <p class="text-center text-muted mb-4">Your instant messaging app for quick communication</p>

      <!-- Login Form -->
      <form @submit.prevent="handleSubmit">
        <div class="mb-3">
          <label for="username" class="form-label">Username</label>
          <input
            type="text"
            id="username"
            v-model="form.username"
            class="form-control"
            placeholder="Enter your username"
            required
          />
        </div>
        <button type="submit" class="btn btn-primary w-100">Login</button>
      </form>
    </div>
  </div>
</template>

<style scoped>
.container {
  background-color: #f8f9fa;
}

.logo-img {
  width: 150px;
  height: 150px;
  border-radius: 50%;
  object-fit: cover;
  border: 3px solid #175156;
}

h2 {
  font-size: 2rem;
  font-weight: bold;
  color: #333;
}

p {
  font-size: 1rem;
}

.card {
  border-radius: 10px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
}

button {
  font-size: 1.1rem;
}
</style>
