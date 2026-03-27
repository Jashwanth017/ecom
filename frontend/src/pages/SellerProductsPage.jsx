import { useEffect, useMemo, useState } from "react";
import { apiClient } from "../api/client";
import { useAuth } from "../auth/AuthContext";

const initialProductState = {
  categoryId: "",
  name: "",
  description: "",
  price: "",
  stockQuantity: "",
  status: "DISABLED_BY_SELLER",
  imageUrl: ""
};

const statusOptions = [
  "ACTIVE",
  "OUT_OF_STOCK",
  "DISABLED_BY_SELLER"
];

function SellerProductsPage() {
  const { accessToken } = useAuth();
  const [products, setProducts] = useState([]);
  const [categories, setCategories] = useState([]);
  const [formState, setFormState] = useState(initialProductState);
  const [editingId, setEditingId] = useState(null);
  const [feedback, setFeedback] = useState({ error: "", success: "" });
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function loadData() {
      setLoading(true);
      try {
        const [productData, categoryData] = await Promise.all([
          apiClient.getSellerProducts(accessToken),
          apiClient.getCategories()
        ]);
        setProducts(productData);
        setCategories(categoryData);
        setFeedback((previous) => ({ ...previous, error: "" }));
      } catch (err) {
        setFeedback({ error: err.message || "Unable to load seller products.", success: "" });
      } finally {
        setLoading(false);
      }
    }

    loadData();
  }, [accessToken]);

  const submitLabel = useMemo(() => (
    editingId ? "Update Product" : "Add Product"
  ), [editingId]);

  function handleChange(field) {
    return (event) => {
      setFormState((previous) => ({
        ...previous,
        [field]: event.target.value
      }));
    };
  }

  function startEdit(product) {
    setEditingId(product.productId);
    setFormState({
      categoryId: String(product.categoryId),
      name: product.name ?? "",
      description: product.description ?? "",
      price: String(product.price ?? ""),
      stockQuantity: String(product.stockQuantity ?? ""),
      status: product.status ?? "DISABLED_BY_SELLER",
      imageUrl: product.imageUrl ?? ""
    });
    setFeedback({ error: "", success: "" });
  }

  function resetForm() {
    setEditingId(null);
    setFormState(initialProductState);
  }

  async function reloadProducts() {
    const data = await apiClient.getSellerProducts(accessToken);
    setProducts(data);
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setFeedback({ error: "", success: "" });

    const payload = {
      categoryId: Number(formState.categoryId),
      name: formState.name,
      description: formState.description,
      price: Number(formState.price),
      stockQuantity: Number(formState.stockQuantity),
      status: formState.status,
      imageUrl: formState.imageUrl || null
    };

    try {
      if (editingId) {
        await apiClient.updateSellerProduct(accessToken, editingId, payload);
        setFeedback({ error: "", success: "Product updated." });
      } else {
        await apiClient.createSellerProduct(accessToken, payload);
        setFeedback({ error: "", success: "Product created." });
      }

      await reloadProducts();
      resetForm();
    } catch (err) {
      setFeedback({ error: err.message || "Unable to save product.", success: "" });
    }
  }

  async function updateStatus(productId, action) {
    setFeedback({ error: "", success: "" });

    try {
      if (action === "enable") {
        await apiClient.enableSellerProduct(accessToken, productId);
      } else {
        await apiClient.disableSellerProduct(accessToken, productId);
      }
      await reloadProducts();
    } catch (err) {
      setFeedback({ error: err.message || "Unable to update status.", success: "" });
    }
  }

  async function saveStock(productId, stockQuantity) {
    setFeedback({ error: "", success: "" });

    try {
      await apiClient.updateSellerProductStock(accessToken, productId, Number(stockQuantity));
      await reloadProducts();
    } catch (err) {
      setFeedback({ error: err.message || "Unable to update stock.", success: "" });
    }
  }

  return (
    <section className="seller-page seller-two-column">
      <form className="panel-card seller-form" onSubmit={handleSubmit}>
        <div className="section-heading">
          <div>
            <h2>{editingId ? "Edit Product" : "Add Product"}</h2>
            <p className="muted">Create and manage products for your store.</p>
          </div>
          {editingId ? (
            <button type="button" className="button button-secondary" onClick={resetForm}>
              Cancel
            </button>
          ) : null}
        </div>

        <label>
          Category
          <select value={formState.categoryId} onChange={handleChange("categoryId")}>
            <option value="">Select category</option>
            {categories.map((category) => (
              <option key={category.categoryId} value={category.categoryId}>
                {category.name}
              </option>
            ))}
          </select>
        </label>

        <label>
          Product Name
          <input type="text" value={formState.name} onChange={handleChange("name")} />
        </label>

        <label>
          Description
          <textarea rows="5" value={formState.description} onChange={handleChange("description")} />
        </label>

        <div className="inline-field-grid">
          <label>
            Price
            <input type="number" min="0" step="0.01" value={formState.price} onChange={handleChange("price")} />
          </label>

          <label>
            Stock
            <input type="number" min="0" value={formState.stockQuantity} onChange={handleChange("stockQuantity")} />
          </label>
        </div>

        <div className="inline-field-grid">
          <label>
            Status
            <select value={formState.status} onChange={handleChange("status")}>
              {statusOptions.map((status) => (
                <option key={status} value={status}>{status}</option>
              ))}
            </select>
          </label>

          <label>
            Image URL
            <input type="text" value={formState.imageUrl} onChange={handleChange("imageUrl")} />
          </label>
        </div>

        {feedback.error ? <p className="form-feedback form-feedback-error">{feedback.error}</p> : null}
        {feedback.success ? <p className="form-feedback form-feedback-success">{feedback.success}</p> : null}

        <button type="submit" className="button button-primary">{submitLabel}</button>
      </form>

      <div className="panel-card">
        <div className="section-heading">
          <div>
            <h2>Your Products</h2>
            <p className="muted">Edit, enable, disable, and update stock.</p>
          </div>
        </div>

        <div className="product-management-grid">
          {loading ? (
            <p className="empty-cell">Loading products...</p>
          ) : products.length === 0 ? (
            <p className="empty-cell">No products added yet.</p>
          ) : (
            products.map((product) => (
              <article key={product.productId} className="seller-product-card">
                <div className="seller-product-head">
                  <div>
                    <span className="product-category">{product.categoryName}</span>
                    <h3>{product.name}</h3>
                  </div>
                  <strong>{formatCurrency(product.price)}</strong>
                </div>

                <p>{product.description}</p>

                <div className="seller-product-meta">
                  <span>Stock: {product.stockQuantity}</span>
                  <span>Status: {product.status}</span>
                </div>

                <div className="seller-product-actions">
                  <button type="button" className="button button-secondary" onClick={() => startEdit(product)}>
                    Edit
                  </button>
                  <button
                    type="button"
                    className="button button-secondary"
                    onClick={() => updateStatus(product.productId, product.status === "DISABLED_BY_SELLER" ? "enable" : "disable")}
                  >
                    {product.status === "DISABLED_BY_SELLER" ? "Enable" : "Disable"}
                  </button>
                </div>

                <div className="stock-inline">
                  <input
                    type="number"
                    min="0"
                    defaultValue={product.stockQuantity}
                    id={`stock-${product.productId}`}
                  />
                  <button
                    type="button"
                    className="button button-secondary"
                    onClick={() => {
                      const value = document.getElementById(`stock-${product.productId}`)?.value ?? product.stockQuantity;
                      saveStock(product.productId, value);
                    }}
                  >
                    Save Stock
                  </button>
                </div>
              </article>
            ))
          )}
        </div>
      </div>
    </section>
  );
}

function formatCurrency(value) {
  return new Intl.NumberFormat("en-US", {
    style: "currency",
    currency: "USD"
  }).format(Number(value));
}

export default SellerProductsPage;
