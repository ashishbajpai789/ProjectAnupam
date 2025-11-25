// Base API URL
const API_URL = "http://localhost:8080/api";
const BASE_URL = "http://localhost:8080";

// ============================================
// AUTHENTICATION FUNCTIONS
// ============================================

// Get token and current user from localStorage
function getToken() {
  return localStorage.getItem("token");
}

function getCurrentUser() {
  return JSON.parse(localStorage.getItem("user") || "{}");
}

function checkRedirect(requiredRole) {
  let token = getToken();
  let currentUser = getCurrentUser();

  // Check if user is logged in
  if (!token || currentUser.userType !== requiredRole) {
    window.location.href = "login.html";
  }
}

// Logout function
async function logout() {
  const token = getToken();
  if (token) {
    showLoader();
    try {
      await fetch(`${API_URL}/auth/logout`, {
        method: "POST",
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });
    } catch (e) {
      console.warn("Failed to revoke token on server", e);
    }
  }

  localStorage.removeItem("token");
  localStorage.removeItem("user");
  window.location.href = "login.html";
  hideLoader();
}

// ============================================
// API CALL FUNCTION
// ============================================

// API helper (supports both authenticated and public calls)
async function apiCall(endpoint, method = "GET", body = null) {
  const token = getToken();
  const options = {
    method,
    headers: {},
  };

  // Add auth token if available
  if (token) {
    options.headers.Authorization = `Bearer ${token}`;
  }

  // Handle body
  if (body && !(body instanceof FormData)) {
    options.headers["Content-Type"] = "application/json";
    options.body = JSON.stringify(body);
  } else if (body instanceof FormData) {
    // If body IS FormData → DO NOT set Content-Type
    options.body = body;
  }

  // Show loader
  showLoader();

  try {
    const response = await fetch(`${API_URL}${endpoint}`, options);

    // Handle 401 only for authenticated routes
    if (response.status === 401 && token) {
      alert("Session expired. Please login again.");
      logout();
      return null;
    }

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    return await response.json();
  } catch (error) {
    console.error("API Call Error:", error);
    return {
      success: false,
      message: error.message,
    };
  } finally {
    hideLoader();
  }
}

// ============================================
// CART FUNCTIONS
// ============================================

function getCart() {
  return JSON.parse(localStorage.getItem("cart") || "[]");
}

function saveCart(cart) {
  localStorage.setItem("cart", JSON.stringify(cart));
  updateCartBadge();
}

function addToCart(productId, quantity = 1) {
  const cart = getCart();
  const existingItem = cart.find((item) => item.productId === productId);

  if (existingItem) {
    existingItem.quantity += quantity;
  } else {
    cart.push({ productId, quantity });
  }

  saveCart(cart);
  showNotification("Product added to cart!", "success");
}

function removeFromCart(productId) {
  const cart = getCart();
  const updatedCart = cart.filter((item) => item.productId !== productId);
  saveCart(updatedCart);
}

function updateCartQuantity(productId, quantity) {
  const cart = getCart();
  const item = cart.find((item) => item.productId === productId);

  if (item) {
    item.quantity = quantity;
    if (item.quantity <= 0) {
      removeFromCart(productId);
    } else {
      saveCart(cart);
    }
  }
}

function clearCart() {
  localStorage.removeItem("cart");
  updateCartBadge();
}

function updateCartBadge() {
  const cart = getCart();
  const totalItems = cart.reduce((sum, item) => sum + item.quantity, 0);
  const badge = document.getElementById("cartBadge");

  if (badge) {
    badge.textContent = totalItems;
    badge.style.display = totalItems > 0 ? "inline-block" : "none";
  }
}

// ============================================
// NOTIFICATION FUNCTION
// ============================================

function showNotification(message, type = "info") {
  // Create notification element
  const notification = document.createElement("div");
  notification.className = `alert alert-${
    type === "success" ? "success" : "danger"
  } position-fixed top-0 start-50 translate-middle-x mt-3`;
  notification.style.cssText =
    "z-index: 9999; min-width: 300px; animation: slideDown 0.3s ease;";
  notification.innerHTML = `
    <i class="fas fa-${
      type === "success" ? "check-circle" : "exclamation-circle"
    } me-2"></i>
    ${message}
  `;

  document.body.appendChild(notification);

  // Remove after 3 seconds
  setTimeout(() => {
    notification.style.animation = "slideUp 0.3s ease";
    setTimeout(() => notification.remove(), 300);
  }, 3000);
}

// ============================================
// LOADER FUNCTIONS
// ============================================

function showLoader() {
  let loader = document.getElementById("globalLoader");
  if (!loader) {
    loader = document.createElement("div");
    loader.id = "globalLoader";
    loader.innerHTML = `
      <div style="position:fixed;top:0;left:0;width:100%;height:100%;
                  background:rgba(0,0,0,0.2);display:flex;
                  align-items:center;justify-content:center;z-index:9999;">
        <div class="loader">
          <!-- SVG loader from Uiverse.io (pink shades) -->
          <svg id="pegtopone" viewBox="0 0 100 100">
            <g>
              <path d="M63,37c-6.7-4-4-27-13-27s-6.3,23-13,27-27,4-27,13,20.3,9,27,13,4,27,13,27,6.3-23,13-27,27-4,27-13-20.3-9-27-13Z" fill="#ff69b4"></path>
            </g>
          </svg>
          <svg id="pegtoptwo" viewBox="0 0 100 100">
            <g>
              <path d="M63,37c-6.7-4-4-27-13-27s-6.3,23-13,27-27,4-27,13,20.3,9,27,13,4,27,13,27,6.3-23,13-27,27-4,27-13-20.3-9-27-13Z" fill="#ff69b4"></path>
            </g>
          </svg>
          <svg id="pegtopthree" viewBox="0 0 100 100">
            <g>
              <path d="M63,37c-6.7-4-4-27-13-27s-6.3,23-13,27-27,4-27,13,20.3,9,27,13,4,27,13,27,6.3-23,13-27,27-4,27-13-20.3-9-27-13Z" fill="#ff69b4"></path>
            </g>
          </svg>
        </div>
      </div>
    `;
    document.body.appendChild(loader);

    // Minimal CSS for pink loader animation
    const style = document.createElement("style");
    style.innerHTML = `
      .loader { width: 100px; height: 100px; position: relative; transform: scale(0.5); }
      .loader svg { position: absolute; animation: float 1s linear infinite; }
      .loader #pegtoptwo { animation-delay: 0.3s; }
      .loader #pegtopthree { animation-delay: 0.6s; }
      @keyframes float {
        0% { transform: translateY(-50px); opacity: 0; }
        50% { transform: translateY(0); opacity: 1; }
        100% { transform: translateY(50px); opacity: 0; }
      }
      @keyframes slideDown {
        from {
          transform: translate(-50%, -100%);
          opacity: 0;
        }
        to {
          transform: translate(-50%, 0);
          opacity: 1;
        }
      }
      @keyframes slideUp {
        from {
          transform: translate(-50%, 0);
          opacity: 1;
        }
        to {
          transform: translate(-50%, -100%);
          opacity: 0;
        }
      }
    `;
    document.head.appendChild(style);
  }
  loader.style.display = "flex";
}

function hideLoader() {
  const loader = document.getElementById("globalLoader");
  if (loader) loader.style.display = "none";
}

// ============================================
// INITIALIZATION
// ============================================

// Initialize cart badge on page load
document.addEventListener("DOMContentLoaded", updateCartBadge);
