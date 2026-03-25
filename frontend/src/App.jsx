import { Navigate, Route, Routes } from "react-router-dom";
import ProtectedRoute from "./auth/ProtectedRoute";
import DashboardLayout from "./layouts/DashboardLayout";
import PublicLayout from "./layouts/PublicLayout";
import AdminCategoriesPage from "./pages/AdminCategoriesPage";
import AdminDashboardPage from "./pages/AdminDashboardPage";
import AdminSellerApprovalsPage from "./pages/AdminSellerApprovalsPage";
import AdminUsersPage from "./pages/AdminUsersPage";
import AuthPage from "./pages/AuthPage";
import HomePage from "./pages/HomePage";
import PlaceholderPage from "./pages/PlaceholderPage";
import SellerCategoriesPage from "./pages/SellerCategoriesPage";
import SellerDashboardPage from "./pages/SellerDashboardPage";
import SellerOrdersPage from "./pages/SellerOrdersPage";
import SellerProductsPage from "./pages/SellerProductsPage";
import SellerProfilePage from "./pages/SellerProfilePage";

function App() {
  return (
    <Routes>
      <Route element={<PublicLayout />}>
        <Route path="/" element={<HomePage />} />
        <Route path="/buyer/login" element={<AuthPage role="buyer" mode="login" />} />
        <Route path="/seller/login" element={<AuthPage role="seller" mode="login" />} />
        <Route path="/admin/login" element={<AuthPage role="admin" mode="login" />} />
        <Route path="/buyer/register" element={<AuthPage role="buyer" mode="register" />} />
        <Route path="/seller/register" element={<AuthPage role="seller" mode="register" />} />
        <Route path="/products" element={<PlaceholderPage title="Product List" description="Public catalog listing page shell." />} />
        <Route path="/products/:productId" element={<PlaceholderPage title="Product Detail" description="Product detail page shell." />} />

        <Route element={<ProtectedRoute allowedRoles={["BUYER"]} />}>
          <Route path="/buyer/cart" element={<PlaceholderPage title="Buyer Cart" description="Buyer cart page shell." />} />
          <Route path="/buyer/orders" element={<PlaceholderPage title="Buyer Orders" description="Buyer orders page shell." />} />
          <Route path="/buyer/profile" element={<PlaceholderPage title="Buyer Profile" description="Buyer profile page shell." />} />
          <Route path="/buyer/addresses" element={<PlaceholderPage title="Buyer Addresses" description="Buyer address management page shell." />} />
        </Route>
      </Route>

      <Route element={<ProtectedRoute allowedRoles={["SELLER"]} />}>
        <Route path="/seller" element={<DashboardLayout areaName="Seller Dashboard" />}>
          <Route path="dashboard" element={<SellerDashboardPage />} />
          <Route path="products" element={<SellerProductsPage />} />
          <Route path="categories" element={<SellerCategoriesPage />} />
          <Route path="profile" element={<SellerProfilePage />} />
          <Route path="orders" element={<SellerOrdersPage />} />
        </Route>
      </Route>

      <Route element={<ProtectedRoute allowedRoles={["ADMIN"]} />}>
        <Route path="/admin" element={<DashboardLayout areaName="Admin Dashboard" />}>
          <Route path="users" element={<AdminUsersPage />} />
          <Route path="seller-approvals" element={<AdminSellerApprovalsPage />} />
          <Route path="categories" element={<AdminCategoriesPage />} />
          <Route path="dashboard" element={<AdminDashboardPage />} />
        </Route>
      </Route>

      <Route path="/home" element={<Navigate to="/" replace />} />
      <Route path="*" element={<PlaceholderPage title="Page Not Found" description="This route is reserved but not implemented yet." />} />
    </Routes>
  );
}

export default App;
