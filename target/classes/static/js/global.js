// Base API URL
const API_URL = "https://projectanupam.onrender.com/api";

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

// API helper (global)
async function apiCall(endpoint, method = "GET", body = null) {
  const token = getToken(); // get fresh token
  const options = {
    method,
    headers: {
      "Content-Type": "application/json",
      Authorization: token ? `Bearer ${token}` : "",
    },
  };

  if (body) {
    options.body = JSON.stringify(body);
  }

  // Show loader
  showLoader();

  try {
    const response = await fetch(`${API_URL}${endpoint}`, options);

    if (response.status === 401) {
      alert("Session expired. Please login again.");
      logout();
      return null;
    }

    return await response.json();
  } catch (error) {
    console.error("API Call Error:", error);
    return null;
  } finally {
    // Hide loader
    hideLoader();
  }
}

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
    `;
    document.head.appendChild(style);
  }
  loader.style.display = "flex";
}

function hideLoader() {
  const loader = document.getElementById("globalLoader");
  if (loader) loader.style.display = "none";
}
