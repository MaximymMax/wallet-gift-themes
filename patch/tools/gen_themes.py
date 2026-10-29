import re, json
def aces(x): return max(0,min(1,(x*(2.51*x+0.03))/(x*(2.43*x+0.59)+0.14)))
def inv(y):
    lo,hi=0.0,50.0
    for _ in range(90):
        m=(lo+hi)/2
        if aces(m)<y: lo=m
        else: hi=m
    return lo
def pre(rgb,gain): return [inv((c/255.0)**2.2)/1.3*g for c,g in zip(rgb,gain)]
f=lambda v:'vec3(%.6f, %.6f, %.6f)'%tuple(v)
cfg=json.load(open('themes.json'))
base=open('base_card_fragment.glsl',encoding='utf-8').read()
dia=open('base_diamond_fragment.glsl',encoding='utf-8').read()
warm_env=['vec3(0.075, 0.064, 0.054)','vec3(0.42, 0.36, 0.30)','vec3(0.36, 0.29, 0.22)','vec3(0.28, 0.21, 0.15)','vec3(0.92, 0.77, 0.60)']
for tid,c in cfg.items():
    bright,dark,gain=c['bright'],c['dark'],c['gain']
    mid=[(a+b)//2 for a,b in zip(bright,dark)]
    s=base
    s=re.sub(r'const vec3 CARD_COLOR = vec3\([^)]*\);','const vec3 CARD_COLOR = '+f(pre(mid,gain))+';',s)
    s=re.sub(r'const vec3 CARD_GRADIENT_DARK = vec3\([^)]*\);','const vec3 CARD_GRADIENT_DARK = '+f(pre(dark,gain))+';',s)
    s=re.sub(r'const vec3 CARD_GRADIENT_BRIGHT = vec3\([^)]*\);','const vec3 CARD_GRADIENT_BRIGHT = '+f(pre(bright,gain))+';',s)
    s=re.sub(r'color \+= vec3\([^)]*\)\s*\n(\s*\* grazing)',lambda m:'color += '+f(c['graze'])+'\n'+m.group(1),s,count=1)
    for old,new in zip(warm_env,c['env']):
        assert old in s,old
        s=s.replace(old,f(new))
    open('beta_src/assets/shaders/wallet_card_fragment_t%s.glsl'%tid,'w',encoding='utf-8').write(s)
    d=dia
    a="vec3 artwork = sampleColor.rgb / max(sampleColor.a, 0.00392157);"
    assert a in d
    d=d.replace(a,a+"\n    artwork = "+c['gem']+";")
    h="color += vec3(0.90, 0.96, 1.0)"
    assert h in d
    d=d.replace(h,"color += "+f(c['hi']))
    open('beta_src/assets/shaders/wallet_card_diamond_fragment_t%s.glsl'%tid,'w',encoding='utf-8').write(d)
    print(tid,c['name'],'mid',mid,'gain',gain)
