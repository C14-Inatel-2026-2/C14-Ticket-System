"""Send a Jenkins pipeline execution report using SMTP configuration from env.

Credentials are supplied via Jenkins Credentials Binding, not hardcoded.
"""

import os
import smtplib
import ssl
from email.message import EmailMessage


def required(name: str) -> str:
    value = os.environ.get(name, "").strip()
    if not value:
        raise RuntimeError(f"Missing required environment variable: {name}")
    return value


def main() -> None:
    host = required("SMTP_HOST")
    port = int(os.getenv("SMTP_PORT", "587"))
    sender = required("SMTP_FROM")
    username = required("SMTP_USERNAME")
    password = required("SMTP_PASSWORD")
    recipients = [s.strip() for s in required("EMAIL_RECIPIENT").split(",") if s.strip()]
    if not recipients:
        raise RuntimeError("EMAIL_RECIPIENT must contain at least one address")

    job = os.getenv("JOB_NAME", "C14 Ticket System")
    number = os.getenv("BUILD_NUMBER", "unknown")
    status = os.getenv("BUILD_STATUS", "UNKNOWN")
    branch = os.getenv("BRANCH_NAME", "unknown")
    url = os.getenv("BUILD_URL", "Not available")
    published = os.getenv("GHCR_PUBLISHED", "false").lower() == "true"

    image = f"ghcr.io/c14-inatel-2026-2/c14-ticket-system:{number}" if published else "Not published in this run"
    message = EmailMessage()
    message["From"] = sender
    message["To"] = ", ".join(recipients)
    message["Subject"] = f"[Jenkins] {job} #{number} - {status}"
    message.set_content(
        f"Job: {job}\n"
        f"Build: #{number}\n"
        f"Result: {status}\n"
        f"Branch: {branch}\n"
        f"Build URL: {url}\n"
        f"GHCR image: {image}\n"
    )

    context = ssl.create_default_context()
    if port == 465:
        with smtplib.SMTP_SSL(host, port, timeout=30, context=context) as smtp:
            smtp.login(username, password)
            smtp.send_message(message)
    else:
        with smtplib.SMTP(host, port, timeout=30) as smtp:
            smtp.ehlo()
            smtp.starttls(context=context)
            smtp.ehlo()
            smtp.login(username, password)
            smtp.send_message(message)

    print(f"Pipeline notification sent to {len(recipients)} recipient(s).")


if __name__ == "__main__":
    main()
