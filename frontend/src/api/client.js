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
