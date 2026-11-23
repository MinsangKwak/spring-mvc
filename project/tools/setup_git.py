from pathlib import Path
import re
import subprocess


ROOT = Path(__file__).resolve().parents[2]
POLICY = ROOT / "project" / ".gitignore"
START = "# >>> spring-mvc shared ignores >>>"
END = "# <<< spring-mvc shared ignores <<<"


def git(*args):
    return subprocess.run(
        ["git", *args], cwd=ROOT, capture_output=True, text=True, check=True
    )


def main():
    exclude_output = git("rev-parse", "--git-path", "info/exclude").stdout.strip()
    exclude = Path(exclude_output)
    if not exclude.is_absolute():
        exclude = ROOT / exclude
    exclude.parent.mkdir(parents=True, exist_ok=True)

    existing = exclude.read_text(encoding="utf-8") if exclude.exists() else ""
    managed = re.compile(
        rf"\n?{re.escape(START)}.*?{re.escape(END)}\n?", re.DOTALL
    )
    preserved = managed.sub("\n", existing).rstrip()
    rules = POLICY.read_text(encoding="utf-8").rstrip()
    block = f"{START}\n{rules}\n{END}"
    updated = f"{preserved}\n\n{block}\n" if preserved else f"{block}\n"

    if updated != existing:
        exclude.write_text(updated, encoding="utf-8")
        print("로컬 Git 제외 규칙을 갱신했습니다.")
    else:
        print("로컬 Git 제외 규칙이 이미 최신입니다.")


if __name__ == "__main__":
    main()
