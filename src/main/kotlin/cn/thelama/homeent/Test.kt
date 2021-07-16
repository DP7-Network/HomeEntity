package cn.thelama.homeent

import org.yaml.snakeyaml.Yaml

fun main() {
    val yaml = Yaml()
    val data = """
warp:
  a: !!cn.thelama.homeent.warp.LocationEntry {description: '', name: a, world: 0,
    x: -108.3308556722686, y: 72.0, z: 34.62935224418467}
    """

    val d = yaml.loadAs(data, MutableMap::class.java)
    println(d)
    println(d::class.java)
}