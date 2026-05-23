import axios from "axios";

export const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || "http://localhost:8080",
  timeout: 9000
});

api.interceptors.request.use((request) => {
  const token = sessionStorage.getItem("accessToken");
  if (token) request.headers.Authorization = `Bearer ${token}`;
  request.headers["X-Correlation-ID"] = crypto.randomUUID();
  return request;
});

export function problem(error) {
  return error?.response?.data?.detail || error?.message || "Request failed";
}
