import { useEffect, useState } from "react";
import { apiClient } from "../api/client";
import { useAuth } from "../auth/AuthContext";

function SellerOrdersPage() {
  const { accessToken } = useAuth();
  const [orders, setOrders] = useState([]);
  const [error, setError] = useState("");

  useEffect(() => {
    async function loadOrders() {
      try {
        const data = await apiClient.getSellerOrderItems(accessToken);
        setOrders(data);
      } catch (err) {
        setError(err.message || "Unable to load seller orders.");
      }
    }

    loadOrders();
  }, [accessToken]);

  return (
    <section className="seller-page">
      <div className="section-heading">
        <div>
          <h2>Seller Orders</h2>
          <p className="muted">Orders that include your products.</p>
        </div>
      </div>

      {error ? <p className="form-feedback form-feedback-error">{error}</p> : null}

      <div className="table-card">
        <table className="seller-table">
          <thead>
            <tr>
              <th>Order</th>
              <th>Product</th>
              <th>Qty</th>
              <th>Total</th>
              <th>Status</th>
              <th>Placed At</th>
            </tr>
          </thead>
          <tbody>
            {orders.length === 0 ? (
              <tr>
                <td colSpan="6" className="empty-cell">No seller orders yet.</td>
              </tr>
            ) : (
              orders.map((item) => (
                <tr key={item.orderItemId}>
                  <td>#{item.orderId}</td>
                  <td>{item.productName}</td>
                  <td>{item.quantity}</td>
                  <td>{formatCurrency(item.lineTotal)}</td>
                  <td>{item.orderStatus}</td>
                  <td>{formatDate(item.placedAt)}</td>
                </tr>
              ))
            )}
          </tbody>
        </table>
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

function formatDate(value) {
  return new Date(value).toLocaleString();
}

export default SellerOrdersPage;
