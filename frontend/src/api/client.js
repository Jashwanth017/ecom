const API_BASE_URL = "http://localhost:8080/api/v1";

async function request(path, { method = "GET", body, token } = {}) {
  const headers = {};

  if (body) {
    headers["Content-Type"] = "application/json";
  }

  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }

  const response = await fetch(`${API_BASE_URL}${path}`, {
    method,
    headers,
    body: body ? JSON.stringify(body) : undefined
  });

  const isJson = response.headers.get("content-type")?.includes("application/json");
  const payload = isJson ? await response.json() : null;

  if (!response.ok) {
    const errorMessage = payload?.message || "Request failed";
    throw new Error(errorMessage);
  }

  return payload;
}

export const apiClient = {
  login(credentials) {
    return request("/auth/login", {
      method: "POST",
      body: credentials
    });
  },

  registerBuyer(data) {
    return request("/auth/register/buyer", {
      method: "POST",
      body: data
    });
  },

  registerSeller(data) {
    return request("/auth/register/seller", {
      method: "POST",
      body: data
    });
  },

  me(accessToken) {
    return request("/auth/me", {
      token: accessToken
    });
  },

  refresh(refreshToken) {
    return request("/auth/refresh", {
      method: "POST",
      body: { refreshToken }
    });
  },

  logout(refreshToken) {
    return request("/auth/logout", {
      method: "POST",
      body: { refreshToken }
    });
  },

  getCategories() {
    return request("/catalog/categories");
  },

  getSellerDashboardSummary(accessToken) {
    return request("/seller/dashboard/summary", {
      token: accessToken
    });
  },

  getAdminDashboardSummary(accessToken) {
    return request("/admin/dashboard/summary", {
      token: accessToken
    });
  },

  getAdminUsers(accessToken, filters = {}) {
    const search = new URLSearchParams();
    if (filters.role) {
      search.set("role", filters.role);
    }
    if (filters.status) {
      search.set("status", filters.status);
    }

    const suffix = search.toString() ? `?${search}` : "";
    return request(`/admin/users${suffix}`, {
      token: accessToken
    });
  },

  getAdminUser(accessToken, userId) {
    return request(`/admin/users/${userId}`, {
      token: accessToken
    });
  },

  banAdminUser(accessToken, userId) {
    return request(`/admin/users/${userId}/ban`, {
      method: "PATCH",
      token: accessToken
    });
  },

  unbanAdminUser(accessToken, userId) {
    return request(`/admin/users/${userId}/unban`, {
      method: "PATCH",
      token: accessToken
    });
  },

  getPendingSellerApprovals(accessToken) {
    return request("/admin/sellers/pending", {
      token: accessToken
    });
  },

  approveSeller(accessToken, sellerProfileId) {
    return request(`/admin/sellers/${sellerProfileId}/approve`, {
      method: "PATCH",
      token: accessToken
    });
  },

  rejectSeller(accessToken, sellerProfileId, reason) {
    return request(`/admin/sellers/${sellerProfileId}/reject`, {
      method: "PATCH",
      body: { reason },
      token: accessToken
    });
  },

  getAdminCategories(accessToken) {
    return request("/admin/categories", {
      token: accessToken
    });
  },

  createAdminCategory(accessToken, data) {
    return request("/admin/categories", {
      method: "POST",
      body: data,
      token: accessToken
    });
  },

  getSellerProfile(accessToken) {
    return request("/seller/profile", {
      token: accessToken
    });
  },

  updateSellerProfile(accessToken, data) {
    return request("/seller/profile", {
      method: "PUT",
      body: data,
      token: accessToken
    });
  },

  getSellerProducts(accessToken) {
    return request("/seller/products", {
      token: accessToken
    });
  },

  getSellerUsedCategories(accessToken) {
    return request("/seller/categories/used", {
      token: accessToken
    });
  },

  createSellerProduct(accessToken, data) {
    return request("/seller/products", {
      method: "POST",
      body: data,
      token: accessToken
    });
  },

  updateSellerProduct(accessToken, productId, data) {
    return request(`/seller/products/${productId}`, {
      method: "PUT",
      body: data,
      token: accessToken
    });
  },

  disableSellerProduct(accessToken, productId) {
    return request(`/seller/products/${productId}/disable`, {
      method: "PATCH",
      token: accessToken
    });
  },

  enableSellerProduct(accessToken, productId) {
    return request(`/seller/products/${productId}/enable`, {
      method: "PATCH",
      token: accessToken
    });
  },

  updateSellerProductStock(accessToken, productId, stockQuantity) {
    return request(`/seller/products/${productId}/stock`, {
      method: "PATCH",
      body: { stockQuantity },
      token: accessToken
    });
  },

  getSellerOrderItems(accessToken) {
    return request("/seller/orders/items", {
      token: accessToken
    });
  }
};
