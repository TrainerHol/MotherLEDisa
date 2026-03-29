from __future__ import annotations

import argparse
import os
import re
from dataclasses import dataclass
from pathlib import Path


@dataclass(frozen=True)
class Version:
    major: int
    minor: int
    patch: int
    prerelease_label: str | None = None
    prerelease_num: int | None = None

    def base(self) -> str:
        return f"{self.major}.{self.minor}.{self.patch}"

    def with_prerelease(self, label: str, num: int) -> "Version":
        return Version(
            major=self.major,
            minor=self.minor,
            patch=self.patch,
            prerelease_label=label,
            prerelease_num=num,
        )

    def without_prerelease(self) -> "Version":
        return Version(major=self.major, minor=self.minor, patch=self.patch)

    def __str__(self) -> str:
        if self.prerelease_label is None:
            return self.base()
        if self.prerelease_num is None:
            return f"{self.base()}-{self.prerelease_label}"
        return f"{self.base()}-{self.prerelease_label}.{self.prerelease_num}"


SEMVER_RE = re.compile(
    r"^(?P<maj>0|[1-9]\d*)\.(?P<min>0|[1-9]\d*)\.(?P<pat>0|[1-9]\d*)"
    r"(?:-(?P<label>[0-9A-Za-z-]+)(?:\.(?P<num>0|[1-9]\d*))?)?$"
)


def parse_version(raw: str) -> Version:
    m = SEMVER_RE.match(raw.strip())
    if m is None:
        raise ValueError(f"Invalid VERSION_NAME: {raw!r}")
    label = m.group("label")
    num_raw = m.group("num")
    num = int(num_raw) if num_raw is not None else None
    return Version(
        major=int(m.group("maj")),
        minor=int(m.group("min")),
        patch=int(m.group("pat")),
        prerelease_label=label,
        prerelease_num=num,
    )


def read_version_properties(path: Path) -> tuple[int, Version]:
    if not path.is_file():
        raise FileNotFoundError(str(path))
    props: dict[str, str] = {}
    for line in path.read_text(encoding="utf-8").splitlines():
        stripped = line.strip()
        if not stripped:
            continue
        if stripped.startswith("#"):
            continue
        if "=" not in stripped:
            continue
        k, v = stripped.split("=", 1)
        props[k.strip()] = v.strip()

    code_raw = props.get("VERSION_CODE")
    name_raw = props.get("VERSION_NAME")
    if code_raw is None or name_raw is None:
        raise ValueError("version.properties must define VERSION_CODE and VERSION_NAME")

    try:
        code = int(code_raw)
    except ValueError as e:
        raise ValueError(f"Invalid VERSION_CODE: {code_raw!r}") from e

    return code, parse_version(name_raw)


def write_version_properties(path: Path, version_code: int, version_name: Version) -> None:
    content = f"VERSION_CODE={version_code}\nVERSION_NAME={version_name}\n"
    path.write_text(content, encoding="utf-8")


def bump_base(v: Version, bump: str) -> Version:
    v0 = v.without_prerelease()
    if bump == "major":
        return Version(major=v0.major + 1, minor=0, patch=0)
    if bump == "minor":
        return Version(major=v0.major, minor=v0.minor + 1, patch=0)
    if bump == "patch":
        return Version(major=v0.major, minor=v0.minor, patch=v0.patch + 1)
    raise ValueError(f"Unknown bump: {bump}")


def apply_prerelease(next_base: Version, current: Version, enabled: bool, label: str) -> Version:
    if not enabled:
        return next_base.without_prerelease()

    if (
        current.without_prerelease().base() == next_base.base()
        and current.prerelease_label == label
        and current.prerelease_num is not None
    ):
        return next_base.with_prerelease(label=label, num=current.prerelease_num + 1)

    return next_base.with_prerelease(label=label, num=1)


def write_github_outputs(outputs: dict[str, str]) -> None:
    out_path = os.environ.get("GITHUB_OUTPUT")
    if not out_path:
        for k, v in outputs.items():
            print(f"{k}={v}")
        return

    p = Path(out_path)
    with p.open("a", encoding="utf-8") as f:
        for k, v in outputs.items():
            f.write(f"{k}={v}\n")


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--file", default="version.properties")
    parser.add_argument("--bump", choices=["patch", "minor", "major"], default="patch")
    parser.add_argument("--prerelease", action="store_true")
    parser.add_argument("--prerelease-label", default="rc")
    args = parser.parse_args()

    props_path = Path(args.file)
    version_code, version_name = read_version_properties(props_path)

    next_code = version_code + 1
    next_base = bump_base(version_name, args.bump)
    next_name = apply_prerelease(
        next_base=next_base,
        current=version_name,
        enabled=bool(args.prerelease),
        label=str(args.prerelease_label),
    )

    write_version_properties(props_path, next_code, next_name)
    write_github_outputs(
        {
            "version_code": str(next_code),
            "version_name": str(next_name),
            "tag": f"v{next_name}",
        }
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
