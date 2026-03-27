import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { apiClient } from "../api/client";
import { useAuth } from "../auth/AuthContext";

function BuyerOrdersPage() {
  const { accessToken } = useAuth();
  const [orders, setOrders] = useState([]);
  const [selectedOrder, setSelectedOrder] = useState(null);
  const [feedback, setFeedback] = useState({ error: "", success: "" });
  const [loading, setLoading] = useState(true);
  const [activeOrderId, setActiveOrderId] = useState(null);

  useEffect(() => {
    loadOrders();
  }, [accessToken]);

  async function loadOrders(preferredOrderId) {
    setLoading(true);

    try {
      const data = await apiClient.getBuyerOrders(accessToken);
      const orderList = data ?? [];
      setOrders(orderList);
      setFeedback((previous) => ({ ...previous, error: "" }));

      const nextOrderId = preferredOrderId ?? orderList[0]?.orderId;
      if (nextOrderId) {
        await handleSelectOrder(nextOrderId, { keepSuccess: true });
      } else {
        setSelectedOrder(null);
      }
    } catch (error) {
      setFeedback({ error: error.message || "Unable to load your orders.", success: "" });
    } finally {
      setLoading(false);
    }
  }

  async function handleSelectOrder(orderId, options = {}) {
    setActiveOrderId(orderId);
    if (!options.keepSuccess) {
      setFeedback({ error: "", success: "" });
    }

    try {
      const data = await apiClient.getBuyerOrder(accessToken, orderId);
      setSelectedOrder(data);
    } catch (error) {
      setFeedback({ error: error.message || "Unable to load order details.", success: "" });
    } finally {
      setActiveOrderId(null);
    }
  }

  return (
    <section className="buyer-page">
      <div className="buyer-page-hero buyer-surface-card">
        <div>
          <p className="eyebrow">Orders</p>
          <h2>Track every order from one timeline</h2>
          <p className="muted">
            Review status, placed date, item count, and inspect each order in detail.
          </p>
        </div>
        <div className="buyer-hero-metrics">
          <div>
            <span>Total Orders</span>
            <strong>{orders.length}</strong>
          </div>
          <div>
            <span>Latest</span>
            <strong>{orders[0] ? formatDate(orders[0].placedAt) : "-"}</strong>
          </div>
        </div>
      </div>

      {feedback.error ? <p className="form-feedback form-feedback-error">{feedback.error}</p> : null}
      {feedback.success ? <p className="form-feedback form-feedback-success">{feedback.success}</p> : null}

      <div className="buyer-layout-grid">
        <div className="table-card">
          <div className="section-heading">
            <div>
              <h2>Order History</h2>
              <p className="muted">Newest orders are shown first.</p>
            </div>
            <button type="button" className="button button-secondary" onClick={() => loadOrders(selectedOrder?.orderId)} disabled={loading}>
              Refresh
            </button>
          </div>

          {loading ? (
            <p className="empty-cell">Loading orders...</p>
          ) : orders.length === 0 ? (
            <div className="buyer-empty-card">
              <h3>No orders yet</h3>
              <p className="muted">Once you place an order from the cart, it will appear here.</p>
              <Link to="/buyer/cart" className="button button-primary">Go to Cart</Link>
            </div>
          ) : (
            <div className="buyer-list">
              {orders.map((order) => (
                <button
                  key={order.orderId}
                  type="button"
                  className={`buyer-order-card ${selectedOrder?.orderId === order.orderId ? "active" : ""}`}
                  onClick={() => handleSelectOrder(order.orderId)}
                  disabled={activeOrderId === order.orderId}
                >
                  <div className="buyer-order-card-head">
                    <div>
                      <h3>Order #{order.orderId}</h3>
                      <p className="muted">{formatDate(order.placedAt)}</p>
                    </div>
                    <span className="buyer-status-badge">{order.status}</span>
                  </div>
                  <div className="buyer-item-meta">
                    <span>{order.itemCount} item{order.itemCount === 1 ? "" : "s"}</span>
                    <span>{formatCurrency(order.totalAmount)}</span>
                  </div>
                </button>
              ))}
            </div>
          )}
        </div>

        <aside className="panel-card buyer-sidebar-stack">
          <div>
            <h2>Order Detail</h2>
            <p className="muted">Select any order to review the products inside it.</p>
          </div>

          {selectedOrder ? (
            <div className="buyer-detail-stack">
              <div className="buyer-detail-header">
                <div>
                  <strong>Order #{selectedOrder.orderId}</strong>
                  <p className="muted">{formatDate(selectedOrder.placedAt)}</p>
                </div>
                <span className="buyer-status-badge">{selectedOrder.status}</span>
              </div>

              <div className="buyer-summary-row">
                <span>Total Amount</span>
                <strong>{formatCurrency(selectedOrder.totalAmount)}</strong>
              </div>

              <div className="buyer-delivery-card">
                <span>Delivery Address</span>
                {selectedOrder.deliveryAddress ? (
                  <>
                    <strong>{selectedOrder.deliveryAddress.addressLine1}</strong>
                    {selectedOrder.deliveryAddress.addressLine2 ? <span>{selectedOrder.deliveryAddress.addressLine2}</span> : null}
                    <span>
                      {selectedOrder.deliveryAddress.city}, {selectedOrder.deliveryAddress.state} {selectedOrder.deliveryAddress.postalCode}
                    </span>
                  </>
                ) : (
                  <span>Delivery address is not available for this order.</span>
                )}
              </div>

              <div className="buyer-detail-list">
                {selectedOrder.items.map((item) => (
                  <div key={item.orderItemId} className="buyer-detail-item">
                    <div>
                      <strong>{item.productName}</strong>
                      <p className="muted">Product #{item.productId}</p>
                    </div>
                    <div className="buyer-detail-item-meta">
                      <span>{item.quantity} x {formatCurrency(item.productPrice)}</span>
                      <strong>{formatCurrency(item.lineTotal)}</strong>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          ) : (
            <p className="empty-cell">Choose an order from the left to inspect its items.</p>
          )}
        </aside>
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

function formatDate(value) {
  if (!value) {
    return "-";
  }

  return new Date(value).toLocaleString();
}

export default BuyerOrdersPage;
