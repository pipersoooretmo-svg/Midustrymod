#!/usr/bin/env python3
"""Synthesises Lumina's sounds and encodes them to OGG (needs numpy and ffmpeg).

usage: gen_sounds.py assets/sounds
"""
import os
import subprocess
import sys
import tempfile
import wave

import numpy as np

RATE = 44100


def t(seconds):
    return np.arange(int(RATE * seconds)) / RATE


def lowpass(x, cutoff):
    """One-pole low-pass; good enough for shaping noise."""
    a = 1 - np.exp(-2 * np.pi * cutoff / RATE)
    out = np.empty_like(x)
    acc = 0.0
    for i, v in enumerate(x):
        acc += a * (v - acc)
        out[i] = acc
    return out


def fade(x, attack=0.002, release=0.01):
    n = len(x)
    env = np.ones(n)
    a, r = max(1, int(RATE * attack)), max(1, int(RATE * release))
    env[:a] = np.linspace(0, 1, a)
    env[-r:] *= np.linspace(1, 0, r)
    return x * env


def normalise(x, peak):
    return x * (peak / max(1e-9, np.max(np.abs(x))))


def hover():
    # short, soft blip that falls slightly in pitch
    x = t(0.055)
    freq = 1500 * np.exp(-x * 9)
    phase = 2 * np.pi * np.cumsum(freq) / RATE
    y = (np.sin(phase) + 0.25 * np.sin(2 * phase)) * np.exp(-x * 70)
    return normalise(fade(y, 0.001, 0.008), 0.45)


def open_():
    # airy upward swish with a faint tone underneath
    rng = np.random.default_rng(1)
    x = t(0.22)
    noise = rng.uniform(-1, 1, len(x))
    bright = noise - lowpass(noise, 900)
    env = np.sin(np.pi * np.clip(x / 0.22, 0, 1)) ** 2
    freq = 420 + 520 * (x / 0.22) ** 1.5
    tone = np.sin(2 * np.pi * np.cumsum(freq) / RATE) * 0.35
    y = (lowpass(bright, 5200) * 0.9 + tone) * env
    return normalise(fade(y, 0.004, 0.03), 0.4)


def thump():
    # sub-bass drop plus a short burst of filtered noise
    rng = np.random.default_rng(2)
    x = t(0.75)
    freq = 34 + 60 * np.exp(-x * 9)
    body = np.sin(2 * np.pi * np.cumsum(freq) / RATE) * np.exp(-x * 5.5)
    body = np.tanh(body * 1.6)
    crack = lowpass(rng.uniform(-1, 1, len(x)), 1400) * np.exp(-x * 16) * 1.6
    rumble = lowpass(rng.uniform(-1, 1, len(x)), 180) * np.exp(-x * 4) * 2.2
    return normalise(fade(body + crack + rumble, 0.002, 0.08), 0.85)


def debris():
    # scattered clicks over a short dusty noise bed
    rng = np.random.default_rng(3)
    x = t(0.5)
    y = lowpass(rng.uniform(-1, 1, len(x)), 2600) * np.exp(-x * 11) * 0.8
    for _ in range(14):
        start = int(rng.uniform(0.0, 0.34) ** 1.3 * RATE)
        length = int(rng.uniform(0.004, 0.016) * RATE)
        click = rng.uniform(-1, 1, length) * np.exp(-np.arange(length) / (length * 0.3))
        y[start:start + length] += lowpass(click, rng.uniform(1500, 5000)) * rng.uniform(0.5, 1.4) * (1 - start / len(x))
    return normalise(fade(y, 0.001, 0.05), 0.6)


def write(path, samples):
    data = (np.clip(samples, -1, 1) * 32767).astype('<i2')
    with tempfile.NamedTemporaryFile(suffix='.wav', delete=False) as tmp:
        wav_path = tmp.name
    try:
        with wave.open(wav_path, 'wb') as w:
            w.setnchannels(1)
            w.setsampwidth(2)
            w.setframerate(RATE)
            w.writeframes(data.tobytes())
        subprocess.run(['ffmpeg', '-loglevel', 'error', '-y', '-i', wav_path, '-c:a', 'libvorbis', '-q:a', '5', path], check=True)
    finally:
        os.remove(wav_path)


def main():
    out = sys.argv[1]
    os.makedirs(out, exist_ok=True)
    for name, fn in (('lumina-hover', hover), ('lumina-open', open_), ('lumina-thump', thump), ('lumina-debris', debris)):
        write(os.path.join(out, name + '.ogg'), fn())
        print('wrote', name)


if __name__ == '__main__':
    main()
