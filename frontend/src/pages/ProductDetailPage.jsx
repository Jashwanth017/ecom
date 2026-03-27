import { useEffect, useState } from "react";
import { Link, useLocation, useParams } from "react-router-dom";
import { apiClient } from "../api/client";
import { useAuth } from "../auth/AuthContext";

function ProductDetailPage() {
  const { productId } = useParams();
  const location = useLocation();
  const { accessToken, user } = useAuth();
  const [product, setProduct] = useState(null);
  const [quantity, setQuantity] = useState(1);
  const [loading, setLoading] = useState(true);
  const [feedback, setFeedback] = useState({ error: "", success: "" });
  const [submitting, setSubmitting] = useState(false);
  const backToCatalogPath = `/products${location.search}`;

  useEffect(() => {
    async function loadProduct() {
      setLoading(true);
      try {
        const data = await apiClient.getCatalogProduct(productId);
        setProduct(data);
        setFeedback({ error: "", success: "" });
      } catch (err) {
        setFeedback({ error: err.message || "Unable to load product details.", success: "" });
      } finally {
        setLoading(false);
      }
    }

    loadProduct();
  }, [productId]);

  async function handleAddToCart() {
    if (!accessToken || user?.role !== "BUYER") {
      setFeedback({
        error: "Login as a buyer to add products to cart.",
        success: ""
      });
      return;
    }

    setSubmitting(true);
    setFeedback({ error: "", success: "" });

    try {
      await apiClient.addBuyerCartItem(accessToken, {
        productId: Number(productId),
        quantity
      });
      setFeedback({ error: "", success: "Product added to cart." });
    } catch (err) {
      setFeedback({ error: err.message || "Unable to add product to cart.", success: "" });
    } finally {
      setSubmitting(false);
    }
  }

  if (loading) {
    return (
      <section className="table-card">
        <p className="empty-cell">Loading product details...</p>
      </section>
    );
  }

  if (!product) {
    return (
      <section className="table-card">
        {feedback.error ? <p className="form-feedback form-feedback-error">{feedback.error}</p> : null}
        <p className="empty-cell">Product not found.</p>
      </section>
    );
  }

  return (
    <section className="buyer-page">
      <div className="product-detail-layout">
        <div className="panel-card product-detail-media">
          {product.imageUrl ? (
            <img src={product.imageUrl} alt={product.name} className="product-detail-image" />
          ) : (
            <div className="product-detail-image product-detail-fallback">{product.name.charAt(0)}</div>
          )}
        </div>

        <div className="panel-card product-detail-content">
          <p className="eyebrow">{product.categoryName}</p>
          <h2>{product.name}</h2>
          <p className="muted">Sold by {product.sellerStoreName}</p>
          <strong className="product-detail-price">{formatCurrency(product.price)}</strong>
          <p>{product.description}</p>

          <div className="catalog-detail-actions">
            <label>
              Quantity
              <input
                type="number"
                min="1"
                value={quantity}
                onChange={(event) => setQuantity(Math.max(1, Number(event.target.value) || 1))}
              />
            </label>
            <button type="button" className="button button-primary" onClick={handleAddToCart} disabled={submitting}>
              {submitting ? "Adding..." : "Add to Cart"}
            </button>
            <Link to={backToCatalogPath} className="button button-secondary">Back to Catalog</Link>
          </div>

          {feedback.error ? <p className="form-feedback form-feedback-error">{feedback.error}</p> : null}
          {feedback.success ? <p className="form-feedback form-feedback-success">{feedback.success}</p> : null}
        </div>
      </div>
    </section>
  );
}

function formatCurrency(value) {
  return new Intl.NumberFormat("en-US", {
    style: "currency",
    currency: "USD"
  }).format(Number(value ?? 0));
}

export default ProductDetailPage;
