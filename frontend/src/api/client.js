const API_BASE_URL = "http://localhost:8080/api/v1";
let authAccessTokenProvider = null;
let authRefreshHandler = null;

export function configureApiClientAuth({ getAccessToken, onUnauthorized } = {}) {
  authAccessTokenProvider = typeof getAccessToken === "function" ? getAccessToken : null;
  authRefreshHandler = typeof onUnauthorized === "function" ? onUnauthorized : null;
}

async function request(path, { method = "GET", body, token } = {}, didRetry = false) {
  const resolvedToken = token ?? authAccessTokenProvider?.() ?? null;
  const headers = {};

  if (body) {
    headers["Content-Type"] = "application/json";
  }

  if (resolvedToken) {
    headers.Authorization = `Bearer ${resolvedToken}`;
  }

  const response = await fetch(`${API_BASE_URL}${path}`, {
    method,
    headers,
    body: body ? JSON.stringify(body) : undefined
  });

  const isJson = response.headers.get("content-type")?.includes("application/json");
  const payload = isJson ? await response.json() : null;

  if (
    response.status === 401 &&
    resolvedToken &&
    !didRetry &&
    !path.startsWith("/auth/") &&
    authRefreshHandler
  ) {
    await authRefreshHandler();
    const nextToken = authAccessTokenProvider?.() ?? null;

    if (nextToken && nextToken !== resolvedToken) {
      return request(path, { method, body, token: nextToken }, true);
    }
  }

  if (!response.ok) {
    const validationErrors = Array.isArray(payload?.errors) ? payload.errors.filter(Boolean) : [];
    const errorMessage = validationErrors[0] || payload?.message || "Request failed";
    const error = new Error(errorMessage);
    error.details = validationErrors;
    error.status = response.status;
    error.payload = payload;
    throw error;
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

  getCatalogProducts(filters = {}) {
    const search = new URLSearchParams();
    if (filters.search) {
      search.set("search", filters.search);
    }
    if (filters.categoryId) {
      search.set("categoryId", String(filters.categoryId));
    }
    if (filters.sort) {
      search.set("sort", filters.sort);
    }

    const suffix = search.toString() ? `?${search}` : "";
    return request(`/catalog/products${suffix}`);
  },

  getCatalogProduct(productId) {
    return request(`/catalog/products/${productId}`);
  },

  getBuyerProfile(accessToken) {
    return request("/buyer/profile", {
      token: accessToken
    });
  },

  updateBuyerProfile(accessToken, data) {
    return request("/buyer/profile", {
      method: "PUT",
      body: data,
      token: accessToken
    });
  },

  getBuyerAddresses(accessToken) {
    return request("/buyer/addresses", {
      token: accessToken
    });
  },

  createBuyerAddress(accessToken, data) {
    return request("/buyer/addresses", {
      method: "POST",
      body: data,
      token: accessToken
    });
  },

  updateBuyerAddress(accessToken, addressId, data) {
    return request(`/buyer/addresses/${addressId}`, {
      method: "PUT",
      body: data,
      token: accessToken
    });
  },

  deleteBuyerAddress(accessToken, addressId) {
    return request(`/buyer/addresses/${addressId}`, {
      method: "DELETE",
      token: accessToken
    });
  },

  getBuyerCart(accessToken) {
    return request("/buyer/cart", {
      token: accessToken
    });
  },

  addBuyerCartItem(accessToken, data) {
    return request("/buyer/cart/items", {
      method: "POST",
      body: data,
      token: accessToken
    });
  },

  updateBuyerCartItem(accessToken, cartItemId, quantity) {
    return request(`/buyer/cart/items/${cartItemId}`, {
      method: "PUT",
      body: { quantity },
      token: accessToken
    });
  },

  removeBuyerCartItem(accessToken, cartItemId) {
    return request(`/buyer/cart/items/${cartItemId}`, {
      method: "DELETE",
      token: accessToken
    });
  },

  getBuyerOrders(accessToken) {
    return request("/buyer/orders", {
      token: accessToken
    });
  },

  getBuyerOrder(accessToken, orderId) {
    return request(`/buyer/orders/${orderId}`, {
      token: accessToken
    });
  },

  placeBuyerOrder(accessToken, addressId) {
    return request("/buyer/orders", {
      method: "POST",
      body: { addressId },
      token: accessToken
    });
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
