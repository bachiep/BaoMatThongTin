export default function AlertBox({ type = 'danger', message }) {
  if (!message) {
    return null;
  }
  return <div className={`alert alert-${type} py-2`}>{message}</div>;
}
